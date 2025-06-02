package io.halkyon.platform.operator.crd;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.crd.generator.annotation.PreserveUnknownFields;
import io.halkyon.platform.operator.model.KubernetesJob;

import java.util.Map;

public class PackageSpec {
    private String name;
    private String description;
    private String tool;
    private String url;
    private String version;

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

    @PreserveUnknownFields
    @JsonPropertyDescription("ValuesObject specifies Helm values to be passed to helm template, defined as a map.")
    private Map<String, Object> valuesObject;

    private KubernetesJob kubernetesJob;

    // Getters and Setters
    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
            "tool='" + tool + '\'' +
            ", url='" + url + '\'' +
            ", version='" + version + '\'' +
            ", values=" + valuesObject +
            ", kubernetesJob=" + kubernetesJob +
            '}';
    }
}
