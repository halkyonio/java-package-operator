package io.halkyon.platform.operator;

import java.util.*;
import io.halkyon.platform.operator.model.Package;

public class PackageUtils {

    /**
     * Orders a list of Package objects based on their 'runAfter' dependencies.
     * Implements Kahn's algorithm for topological sort.
     *
     * @param packages The list of Package objects to order.
     * @return A new List containing the Package objects in topological order.
     * @throws IllegalArgumentException if a dependency is specified but not found in the input list.
     * @throws IllegalStateException if a circular dependency is detected.
     */
    public static LinkedList<Package> orderPackages(List<Package> packages) {
        if (packages == null || packages.isEmpty()) {
            return new LinkedList<>();
        }

        // 1. Data Structures for the graph representation
        Map<String, Package> packageMap = new HashMap<>();
        // Map: Package name -> count of dependencies it has (in-degree)
        Map<String, Integer> inDegree = new HashMap<>();
        // Map: Package name -> list of package names that depend on it (adjacency list)
        // An edge from A -> B means B depends on A, so A must come before B.
        Map<String, List<String>> adj = new HashMap<>();

        // Initialize maps for all packages
        for (Package pkg : packages) {
            packageMap.put(pkg.getName(), pkg);
            inDegree.put(pkg.getName(), 0); // Start with 0 in-degree for all
            adj.put(pkg.getName(), new ArrayList<>()); // Initialize empty list for dependents
        }

        // 2. Populate in-degrees and adjacency list based on 'runAfter'
        for (Package pkg : packages) {
            pkg.getRunAfter().ifPresentOrElse(depName -> {
                // If 'pkg' runs after 'depName', then 'depName' is a prerequisite for 'pkg'.
                // This means there's a directed edge from 'depName' to 'pkg'.
                if (!packageMap.containsKey(depName)) {
                    throw new IllegalArgumentException("Dependency '" + depName + "' for package '" + pkg.getName() + "' not found in the provided list.");
                }
                adj.get(depName).add(pkg.getName()); // Add edge: depName -> pkg.getName()
                inDegree.compute(pkg.getName(), (k, v) -> v + 1); // Increment in-degree of 'pkg'
            },() -> System.out.println("Not found"));
        }

        // 3. Find initial "root" nodes (nodes with no incoming dependencies)
        Queue<String> queue = new LinkedList<>(); // LinkedList implements Queue interface
        for (String pkgName : inDegree.keySet()) {
            if (inDegree.get(pkgName) == 0) {
                queue.offer(pkgName); // Add to queue if no dependencies
            }
        }

        // 4. Process nodes in topological order (BFS)
        LinkedList<Package> linkedList = new LinkedList<>();
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
        if (nodesVisited != packages.size()) {
            throw new IllegalStateException("Circular dependency detected among packages. Cannot establish a valid order.");
        }

        return linkedList;
    }
}