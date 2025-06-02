package io.halkyon.platform.operator.crd;

import java.util.List;

public class PlatformSpec {
    private String version;
    private String description;
    private List<PackageCR> packageCRS;
    private String name;

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

    public List<PackageCR> getPackages() {
        return packageCRS;
    }

    public void setPackages(List<PackageCR> packageCRS) {
        this.packageCRS = packageCRS;
    }
}
