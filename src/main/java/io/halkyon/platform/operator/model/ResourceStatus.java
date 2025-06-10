package io.halkyon.platform.operator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceStatus {

    private List<Condition> conditions;

    public ResourceStatus() {
        this.conditions = new ArrayList<>(); // Initialize to avoid NullPointerException
    }

    public ResourceStatus(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public void addCondition(Condition condition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(condition);
    }

    @Override
    public String toString() {
        return "ResourceStatus{" +
            "conditions=" + conditions +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceStatus that = (ResourceStatus) o;
        return Objects.equals(conditions, that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditions);
    }
}
