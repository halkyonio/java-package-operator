package io.halkyon.platform.operator.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Condition {

    private OffsetDateTime lastTransitionTime;
    private OffsetDateTime lastUpdateTime;
    private String message;
    private String reason;
    private String status; // Kept as String ("True", "False") as in YAML
    private String type;

    public Condition() {
    }

    public Condition(OffsetDateTime lastTransitionTime, OffsetDateTime lastUpdateTime,
                     String message, String reason, String status, String type) {
        this.lastTransitionTime = lastTransitionTime;
        this.lastUpdateTime = lastUpdateTime;
        this.message = message;
        this.reason = reason;
        this.status = status;
        this.type = type;
    }

    public OffsetDateTime getLastTransitionTime() {
        return lastTransitionTime;
    }

    public void setLastTransitionTime(OffsetDateTime lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    public OffsetDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(OffsetDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Condition{" +
            "lastTransitionTime=" + lastTransitionTime +
            ", lastUpdateTime=" + lastUpdateTime +
            ", message='" + message + '\'' +
            ", reason='" + reason + '\'' +
            ", status='" + status + '\'' +
            ", type='" + type + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return Objects.equals(lastTransitionTime, condition.lastTransitionTime) &&
            Objects.equals(lastUpdateTime, condition.lastUpdateTime) &&
            Objects.equals(message, condition.message) &&
            Objects.equals(reason, condition.reason) &&
            Objects.equals(status, condition.status) &&
            Objects.equals(type, condition.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastTransitionTime, lastUpdateTime, message, reason, status, type);
    }
}

