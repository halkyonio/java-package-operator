package io.halkyon.platform.operator.crd.pkg;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.crd.generator.annotation.PreserveUnknownFields;
import io.halkyon.platform.operator.crd.KubernetesJob;

import java.util.Map;

public class PackageSpec {
    private String name;
    private String description;
    private String tool;
    private String url;
    private String version;

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
