package io.halkyon.platform.operator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.Optional;

public class PackageDefinition {
    private String name;
    private String description;
    private String id;
    private Pipeline pipeline;
    private String runAfter;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public PackageDefinition withName(String name) {
        this.name = name;
        return this;
    }

    public PackageDefinition withRunAfter(String runAfter) {
        this.runAfter = runAfter;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private Map<String, Object> valuesObject;
    private KubernetesJob kubernetesJob;

    @JsonIgnore
    public Optional<String> getRunAfter() {
        return Optional.ofNullable(runAfter);
    }

    public void setRunAfter(String  runAfter) {
        this.runAfter = runAfter;
    }

    public Map<String, Object> getValuesObject() {
        return valuesObject;
    }

    public void setValuesObject(Map<String, Object> valuesObject) {
        this.valuesObject = valuesObject;
    }

    public KubernetesJob getKubernetesJob() {
        return kubernetesJob;
    }

    public void setKubernetesJob(KubernetesJob kubernetesJob) {
        this.kubernetesJob = kubernetesJob;
    }

    @Override
    public String toString() {
        return "PackageSpec{" +
            "values=" + valuesObject +
            ", kubernetesJob=" + kubernetesJob +
            '}';
    }
}
