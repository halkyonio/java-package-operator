package io.halkyon.platform.operator.crd;

import io.halkyon.platform.operator.model.PackageDefinition;
import java.util.List;

public class PlatformSpec {
    private String name;
    private String description;
    private String version;
    private List<PackageDefinition> packageDefinitions;

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

    public List<PackageDefinition> getPackages() {
        return packageDefinitions;
    }

    public void setPackages(List<PackageDefinition> packageDefinitions) {
        this.packageDefinitions = packageDefinitions;
    }
}
