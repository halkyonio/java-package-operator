package io.halkyon.platform.operator.crd;

import java.util.LinkedList;
import java.util.List;
import io.halkyon.platform.operator.model.Package;

public class PlatformStatus {
    private String phase;
    private String message;
    private List<Package> packages;

    public PlatformStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(LinkedList<Package> packages) {
        this.packages = packages;
    }

    @Override
    public String toString() {
        return "PlatformStatus{" +
            "phase='" + phase + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
