package io.halkyon.platform.operator.model;

import java.util.Map;
import java.util.Optional;

public class Package {
    private String name;
    private String description;
    private Pipeline pipeline;
    private String runAfter;

    public Package(String name, String runAfter) {
        this.name = name;
        this.runAfter = runAfter;
    }

    public Package(String name) {
        this.name = name;
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


    public Optional<String>  getRunAfter() {
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
