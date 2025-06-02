package io.halkyon.platform.operator.crd;

import java.util.LinkedList;
import java.util.List;
import io.halkyon.platform.operator.model.Package;

public class PlatformStatus {
    private String phase;
    private String message;
    private Package packageToProcess;
    private List<Package> packages;

    public PlatformStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    public Package getPackageToProcess() {
        return packageToProcess;
    }

    public void setPackageToProcess(Package packageToProcess) {
        this.packageToProcess = packageToProcess;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public List<Package> getPackages() {
        return packages;
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
