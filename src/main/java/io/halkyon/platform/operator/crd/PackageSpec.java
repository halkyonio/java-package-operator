package io.halkyon.platform.operator.crd;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.crd.generator.annotation.PreserveUnknownFields;
import io.halkyon.platform.operator.model.KubernetesJob;

import java.util.Map;

public class PackageSpec {
    private String name;
    private String description;
    private String tool;
    private String repoUrl;
    private String version;

    @PreserveUnknownFields
    @JsonPropertyDescription("ValuesObject specifies Helm values to be passed to helm template, defined as a map.")
    private Map<String, Object> values;
    private KubernetesJob kubernetesJob;

    private String runAfter;

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

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRunAfter() {
        return runAfter;
    }

    public void setRunAfter(String runAfter) {
        this.runAfter = runAfter;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
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
            ", url='" + repoUrl + '\'' +
            ", version='" + version + '\'' +
            ", values=" + values +
            ", kubernetesJob=" + kubernetesJob +
            '}';
    }
}
