package io.halkyon.platform;

import io.halkyon.platform.operator.PackageUtils;
import io.halkyon.platform.operator.model.Package;
import io.halkyon.platform.operator.model.Platform;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PackageUtilsTest {
    @Test
    public void basicOrderingTest() {
        List<Package> pkgs = new ArrayList<>();
        pkgs.add(new Package("3", "2"));
        pkgs.add(new Package("2", "1"));
        pkgs.add(new Package("1")); // No runAfter, so it's a root

        Platform platform = new Platform(pkgs);

        try {
            System.out.println("--- Basic ordering ---");
            List<Package> pkg = PackageUtils.orderPackages(platform.getPackages());
            System.out.println("Ordered Packages (Expected: 1, 2, 3):");
            pkg.forEach(p -> System.out.println(p.getName()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage());
        }
    }

    @Test
    public void moreComplexOrderingTest() {
        List<Package> pkgs = new ArrayList<>();
        pkgs.add(new Package("C", "B"));
        pkgs.add(new Package("A")); // Independent root
        pkgs.add(new Package("B", "A"));
        pkgs.add(new Package("Z")); // Independent root

        Platform platform = new Platform(pkgs);

        try {
            System.out.println("--- Ordering Example 2 ---");
            List<Package> pkg = PackageUtils.orderPackages(platform.getPackages());
            System.out.println("Ordered Packages (Expected: A, B, C; Z can be anywhere relative to A,B,C block):");
            pkg.forEach(p -> System.out.println(p.getName()));
        } catch (IllegalArgumentException |
                 IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage());
        }
    }

    @Test
    public void circularDependencyTest() {
        List<Package> circularPackages = new ArrayList<>();
        circularPackages.add(new Package("P1", "P3")); // P1 runs after P3
        circularPackages.add(new Package("P2", "P1")); // P2 runs after P1
        circularPackages.add(new Package("P3", "P2")); // P3 runs after P2

        Platform platform = new Platform(circularPackages);
        try {
            System.out.println("--- Ordering Example 3: Circular Dependency ---");
            PackageUtils.orderPackages(platform.getPackages());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage()); // Expected output
        }
    }

    @Test
    public void missingDependencyTest() {
        List<Package> missingDepPackages = new ArrayList<>();
        missingDepPackages.add(new Package("P_A", "P_MISSING"));

        Platform platform = new Platform(missingDepPackages);
        try {
            System.out.println("--- Ordering Example 4: Missing Dependency ---");
            PackageUtils.orderPackages(platform.getPackages());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage()); // Expected output
        }
    }
}