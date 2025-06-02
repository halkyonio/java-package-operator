package io.halkyon.platform.operator.crd;

public class PlatformStatus {
    private String phase;
    private String message;

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

    @Override
    public String toString() {
        return "PlatformStatus{" +
            "phase='" + phase + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
