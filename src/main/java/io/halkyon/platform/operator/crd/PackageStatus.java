package io.halkyon.platform.operator.crd;

public class PackageStatus {
    private String phase;
    private String message;
    private String name;
    private String installationStatus;

    public PackageStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstallationStatus() {
        return installationStatus;
    }

    public void setInstallationStatus(String installationStatus) {
        this.installationStatus = installationStatus;
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

    @Override
    public String toString() {
        return "PackageStatus{" +
            "phase='" + phase + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
