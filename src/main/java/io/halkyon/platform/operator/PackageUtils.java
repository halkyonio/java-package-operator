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
    public final static String PACKAGE_ORDER_LABEL = "io.halkyon.package.order";
    public final static String INSTALLATION_FAILED = "installation failed";

    public static List<String> generatePodCommand(Step step, Mode action) {
        String result = "";

        switch (action) {
            case Mode.WAIT_FOR:
                result = Templates.waitscript(step).render();
                break;
            case Mode.MANIFEST_INSTALL:
                result = Templates.manifestscript(step).render();
                break;
            case Mode.MANIFEST_UNINSTALL:
                result = Templates.uninstallmanifestscript(step).render();
                break;
            case Mode.HELM_INSTALL:
                result = Templates.helmscript(step).render();
                break;
            case Mode.HELM_UNINSTALL:
                result = Templates.uninstallhelmscript(step).render();
                break;
            case Mode.SCRIPT_INSTALL, Mode.SCRIPT_UNINSTALL:
                result = step.getScript();
                break;
        }

        LOG.debug(result);

        return Stream.concat(
                Stream.of("/bin/bash", "-exc"),
                Stream.of(result))
            .collect(Collectors.toList());
    }

    /**
     * Determines the appropriate action Mode for a given Step, considering cleanup logic first.
     *
     * @param step    The Step object to evaluate.
     * @param cleanup A boolean flag indicating if the overall operation is a cleanup.
     * @return An Optional containing the determined Mode, or empty if no specific mode is matched.
     */
    public static Optional<Mode> determineAction(Step step, boolean cleanup) {
        if (step == null || step.getName() == null) {
            // Log a warning or throw an IllegalArgumentException if a null step/name is truly an error
            return Optional.empty();
        }

        // --- 1. Cleanup Overrides (Highest Priority for Specific Cleanup Types) ---
        if (cleanup) {
            if (step.getHelm() != null) {
                // If in cleanup mode and the step has Helm configuration, it's a Helm uninstall
                return Optional.of(Mode.HELM_UNINSTALL);
            }
            if (step.getManifest() != null) {
                // If in cleanup mode and the step has Manifest configuration, it's a Manifest uninstall
                return Optional.of(Mode.MANIFEST_UNINSTALL); // <-- New cleanup condition
            }
            if (step.getScript() != null) {
                return Optional.of(Mode.SCRIPT_UNINSTALL);
            }
        }

        // --- 2. Standard Operation Modes (if not in a specific cleanup path) ---
        if (step.getName().startsWith("init") && (step.getWaitCondition() != null || step.getScript() != null)) {
            return Optional.of(Mode.WAIT_FOR);
        }

        if (step.getName().startsWith("install") && step.getHelm() != null) {
            return Optional.of(Mode.HELM_INSTALL);
        }

        if (step.getName().startsWith("install") && step.getManifest() != null) {
            return Optional.of(Mode.MANIFEST_INSTALL);
        }

        if (step.getName().startsWith("install") && step.getScript() != null) {
            return Optional.of(Mode.SCRIPT_INSTALL);
        }

        // If none of the above conditions are met, return an empty Optional.
        // This signifies that no specific, pre-defined action mode was determined for this step.
        return Optional.empty();
    }

    public static LinkedHashMap<String, String> createResourceLabels(Package pkg) {
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();
        labels.put(PACKAGE_LABEL_SELECTOR, pkg.getMetadata().getName());
        return labels;
    }

    public static LinkedHashMap<String, String> createPackageLabels(Package pkg, Integer order) {
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();
        labels.put(PACKAGE_LABEL_SELECTOR, pkg.getMetadata().getName());
        labels.put(PACKAGE_ORDER_LABEL, String.valueOf(order));
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

    /**
     * Sorts a list of Packages in reversing order based on the "io.halkyon.package.order" label.
     * Packages without a valid numerical order label will be treated as the lowest priority (appear last in reverse order).
     *
     * @param packages The list of Package objects to sort.
     * @return A new List containing the sorted Packages.
     */
    public static List<Package> sortPackagesByOrderLabel(Set<Package> packages) {
        if (packages == null || packages.isEmpty()) {
            return Collections.emptyList();
        }

        return packages.stream()
            .sorted(Comparator.comparing(
                    obj -> {
                        Package pkg = (Package) obj;
                        String orderString = pkg.getMetadata().getLabels().get(PACKAGE_ORDER_LABEL);

                        if (orderString == null || orderString.isBlank()) { // Check for null or blank
                            return Integer.MIN_VALUE;
                        }

                        try {
                            return Integer.parseInt(orderString);
                        } catch (NumberFormatException e) {
                            LOG.warn("!!! Invalid number format for package order label on package '" + pkg.getMetadata().getName() + "': " + orderString);
                            return Integer.MIN_VALUE; // Handle parsing error: treat as lowest priority
                        }
                    },
                    Comparator.naturalOrder()
                )
                .reversed())
            .collect(Collectors.toList());
    }
}