package io.halkyon.platform.operator.model;

import java.util.List;

// Assuming a simple Platform class structure for context
public class Platform {
    private List<PackageDefinition> packageDefinitions;

    public Platform(List<PackageDefinition> packageDefinitions) {
        this.packageDefinitions = packageDefinitions;
    }

    public List<PackageDefinition> getPackages() {
        return packageDefinitions;
    }
}