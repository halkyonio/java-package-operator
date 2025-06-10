package io.halkyon.platform.operator.crd;

import java.util.ArrayList;
import java.util.List;

import io.halkyon.platform.operator.model.Condition;
import io.halkyon.platform.operator.model.PackageDefinition;

public class PlatformStatus {
    private String message;
    private PackageDefinition packageDefinitionToProcess;
    private List<PackageDefinition> packageDefinitions;
    private List<Condition> conditions;

    public PlatformStatus() {
        this.conditions = new ArrayList<>(); // Initialize to avoid NullPointerException
    }

    public PlatformStatus(List<Condition> conditions) {
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
            "conditions=" + conditions +
            '}';
    }
}
