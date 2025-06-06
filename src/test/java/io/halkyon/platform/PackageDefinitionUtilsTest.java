package io.halkyon.platform;

import io.halkyon.platform.operator.PackageUtils;
import io.halkyon.platform.operator.model.PackageDefinition;
import io.halkyon.platform.operator.model.Platform;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PackageDefinitionUtilsTest {
    @Test
    public void basicOrderingTest() {
        List<PackageDefinition> pkgs = new ArrayList<>();
        pkgs.add(new PackageDefinition().withName("3").withRunAfter("2"));
        pkgs.add(new PackageDefinition().withName("2").withRunAfter("1"));
        pkgs.add(new PackageDefinition().withName("1"));

        Platform platform = new Platform(pkgs);

        try {
            System.out.println("--- Basic ordering ---");
            List<PackageDefinition> pkg = PackageUtils.orderPackages(platform.getPackages());
            System.out.println("Ordered Packages (Expected: 1, 2, 3):");
            pkg.forEach(p -> System.out.println(p.getName()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage());
        }
    }

    @Test
    public void moreComplexOrderingTest() {
        List<PackageDefinition> pkgs = new ArrayList<>();
        pkgs.add(new PackageDefinition().withName("C").withRunAfter("B"));
        pkgs.add(new PackageDefinition().withName("A")); // Independent root
        pkgs.add(new PackageDefinition().withName("B").withRunAfter("A"));
        pkgs.add(new PackageDefinition().withName("Z")); // Independent root

        Platform platform = new Platform(pkgs);

        try {
            System.out.println("--- Ordering Example 2 ---");
            List<PackageDefinition> pkg = PackageUtils.orderPackages(platform.getPackages());
            System.out.println("Ordered Packages (Expected: A, B, C; Z can be anywhere relative to A,B,C block):");
            pkg.forEach(p -> System.out.println(p.getName()));
        } catch (IllegalArgumentException |
                 IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage());
        }
    }

    @Test
    public void circularDependencyTest() {
        List<PackageDefinition> circularPackageDefinitions = new ArrayList<>();
        circularPackageDefinitions.add(new PackageDefinition().withName("P1").withRunAfter("P3")); // P1 runs after P3
        circularPackageDefinitions.add(new PackageDefinition().withName("P2").withRunAfter("P1")); // P2 runs after P1
        circularPackageDefinitions.add(new PackageDefinition().withName("P3").withRunAfter("P2")); // P3 runs after P2

        Platform platform = new Platform(circularPackageDefinitions);
        try {
            System.out.println("--- Ordering Example 3: Circular Dependency ---");
            PackageUtils.orderPackages(platform.getPackages());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage()); // Expected output
        }
    }

    @Test
    public void missingDependencyTest() {
        List<PackageDefinition> missingDepPackageDefinitions = new ArrayList<>();
        missingDepPackageDefinitions.add(new PackageDefinition().withName("P_A").withRunAfter("P_MISSING"));

        Platform platform = new Platform(missingDepPackageDefinitions);
        try {
            System.out.println("--- Ordering Example 4: Missing Dependency ---");
            PackageUtils.orderPackages(platform.getPackages());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error ordering packages: " + e.getMessage()); // Expected output
        }
    }
}