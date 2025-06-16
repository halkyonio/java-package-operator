package io.halkyon.platform.operator;

import io.halkyon.platform.operator.crd.Package;
import io.halkyon.platform.operator.model.PackageDefinition;
import io.halkyon.platform.operator.model.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PackageUtils.class);

    public final static String INSTALLATION_SUCCEEDED = "installation succeeded";
    public final static String PACKAGE_LABEL_SELECTOR = "io.halkyon.package";
    public final static String INSTALLATION_FAILED = "installation failed";

    public static List<String> generatePodCommandFromScript(String script) {
        return Stream.concat(
                Stream.of("/bin/bash", "-exc"),
                Stream.of(script))
            .collect(Collectors.toList());
    }

    public static List<String> generatePodCommandFromTemplate(Step step, Mode action) {
        String result = "";

        switch (action) {
            case Mode.WAIT_FOR:
                result = Templates.waitscript(step).render();
                break;
            case Mode.INIT, Mode.INSTALL:
                result = Templates.helmscript(step).render();
                break;
            case Mode.UNINSTALL:
                result = Templates.uninstallhelmscript(step).render();
                break;
        }

        LOG.debug(result);

        return Stream.concat(
                Stream.of("/bin/bash", "-exc"),
                Stream.of(result))
            .collect(Collectors.toList());
    }

    public static Mode actionToDo(Step step, boolean cleanup) {
        if (cleanup) {
            return Mode.UNINSTALL;
        } else if (step.getName().startsWith("init") && step.getWaitCondition() != null) {
            return Mode.WAIT_FOR;
        } else if (step.getName().startsWith("install") && step.getHelm() != null) {
            return Mode.INSTALL;
        } else {
            throw new RuntimeException("Invalid step action !");
        }
}

public static LinkedHashMap<String, String> createPackageLabels(Package pkg) {
    LinkedHashMap<String, String> labels = new LinkedHashMap<>();
    labels.put(PACKAGE_LABEL_SELECTOR, pkg.getMetadata().getName());
    return labels;
}

/**
 * Orders a list of Package objects based on their 'runAfter' dependencies.
 * Implements Kahn's algorithm for topological sort.
 *
 * @param packageDefinitions The list of Package objects to order.
 * @return A new List containing the Package objects in topological order.
 * @throws IllegalArgumentException if a dependency is specified but not found in the input list.
 * @throws IllegalStateException    if a circular dependency is detected.
 */
public static LinkedList<PackageDefinition> orderPackages(List<PackageDefinition> packageDefinitions) {
    if (packageDefinitions == null || packageDefinitions.isEmpty()) {
        return new LinkedList<>();
    }

    // 1. Data Structures for the graph representation
    Map<String, PackageDefinition> packageMap = new HashMap<>();
    // Map: Package name -> count of dependencies it has (in-degree)
    Map<String, Integer> inDegree = new HashMap<>();
    // Map: Package name -> list of package names that depend on it (adjacency list)
    // An edge from A -> B means B depends on A, so A must come before B.
    Map<String, List<String>> adj = new HashMap<>();

    // Initialize maps for all packages
    for (PackageDefinition pkg : packageDefinitions) {
        packageMap.put(pkg.getName(), pkg);
        inDegree.put(pkg.getName(), 0); // Start with 0 in-degree for all
        adj.put(pkg.getName(), new ArrayList<>()); // Initialize empty list for dependents
    }

    // 2. Populate in-degrees and adjacency list based on 'runAfter'
    for (PackageDefinition pkg : packageDefinitions) {
        pkg.getRunAfter().ifPresentOrElse(depName -> {
            // If 'pkg' runs after 'depName', then 'depName' is a prerequisite for 'pkg'.
            // This means there's a directed edge from 'depName' to 'pkg'.
            if (!packageMap.containsKey(depName)) {
                throw new IllegalArgumentException("Dependency '" + depName + "' for package '" + pkg.getName() + "' not found in the provided list.");
            }
            adj.get(depName).add(pkg.getName()); // Add edge: depName -> pkg.getName()
            inDegree.compute(pkg.getName(), (k, v) -> v + 1); // Increment in-degree of 'pkg'
        }, () -> LOG.warn("RunAfter not defined for the package: {}", pkg.getName()));
    }

    // 3. Find initial "root" nodes (nodes with no incoming dependencies)
    Queue<String> queue = new LinkedList<>(); // LinkedList implements Queue interface
    for (String pkgName : inDegree.keySet()) {
        if (inDegree.get(pkgName) == 0) {
            queue.offer(pkgName); // Add to queue if no dependencies
        }
    }

    // 4. Process nodes in topological order (BFS)
    LinkedList<PackageDefinition> linkedList = new LinkedList<>();
    int nodesVisited = 0;

    while (!queue.isEmpty()) {
        String currentPkgName = queue.poll();
        linkedList.add(packageMap.get(currentPkgName)); // Add to result
        nodesVisited++;

        // For each package that depends on the current one
        for (String dependentPkgName : adj.getOrDefault(currentPkgName, Collections.emptyList())) {
            inDegree.compute(dependentPkgName, (k, v) -> v - 1); // Decrement its in-degree
            if (inDegree.get(dependentPkgName) == 0) {
                queue.offer(dependentPkgName); // If its in-degree becomes 0, add to queue
            }
        }
    }

    // 5. Check for cycles
    if (nodesVisited != packageDefinitions.size()) {
        throw new IllegalStateException("Circular dependency detected among packages. Cannot establish a valid order.");
    }

    return linkedList;
}

public static Mode getMode(String value) {
    if (value == null) {
        throw new IllegalArgumentException("Input string cannot be null");
    }
    try {
        return Mode.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid Mode: " + value, e);
    }
}
}