package io.halkyon.platform.operator.crd;

import io.halkyon.platform.operator.model.Package;
import java.util.List;

public class PlatformSpec {
    private String name;
    private String description;
    private String version;
    private List<Package> packages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }
}
