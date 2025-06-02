package io.halkyon.platform.operator.model;

import java.util.List;

// Assuming a simple Platform class structure for context
public class Platform {
    private List<Package> packages;

    public Platform(List<Package> packages) {
        this.packages = packages;
    }

    public List<Package> getPackages() {
        return packages;
    }
}