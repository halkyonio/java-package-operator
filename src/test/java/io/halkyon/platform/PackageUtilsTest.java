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
        pkgs.add(new Package().withName("3").withRunAfter("2"));
        pkgs.add(new Package().withName("2").withRunAfter("1"));
        pkgs.add(new Package().withName("1"));

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
        pkgs.add(new Package().withName("C").withRunAfter("B"));
        pkgs.add(new Package().withName("A")); // Independent root
        pkgs.add(new Package().withName("B").withRunAfter("A"));
        pkgs.add(new Package().withName("Z")); // Independent root

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
        circularPackages.add(new Package().withName("P1").withRunAfter("P3")); // P1 runs after P3
        circularPackages.add(new Package().withName("P2").withRunAfter("P1")); // P2 runs after P1
        circularPackages.add(new Package().withName("P3").withRunAfter("P2")); // P3 runs after P2

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
        missingDepPackages.add(new Package().withName("P_A").withRunAfter("P_MISSING"));

        Platform platform = new Platform(missingDepPackages);
        try {
            System.out.println("--- Ordering Example 4: Missing Dependency ---");
            PackageUtils.orderPackages(platform.getPackages());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage()); // Expected output
        }
    }
}