package io.halkyon.platform.operator.crd;

import java.util.List;
import io.halkyon.platform.operator.model.PackageDefinition;

public class PlatformStatus {
    private String phase;
    private String message;
    private PackageDefinition packageDefinitionToProcess;
    private List<PackageDefinition> packageDefinitions;

    public PlatformStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    public PackageDefinition getPackageToProcess() {
        return packageDefinitionToProcess;
    }

    public void setPackageToProcess(PackageDefinition packageDefinitionToProcess) {
        this.packageDefinitionToProcess = packageDefinitionToProcess;
    }

    public void setPackages(List<PackageDefinition> packageDefinitions) {
        this.packageDefinitions = packageDefinitions;
    }

    public List<PackageDefinition> getPackages() {
        return packageDefinitions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PlatformStatus{" +
            "phase='" + phase + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
