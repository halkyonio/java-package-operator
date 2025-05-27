package io.halkyon.crd.platform;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.crd.generator.annotation.PreserveUnknownFields;
import io.halkyon.crd.KubernetesJob;

import java.util.Map;

public class Package {
    private String name;
    private String description;
    private Pipeline pipeline;

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

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public String toString() {
        return "Package{" +
            "name='" + name + '\'' +
            ", descrption='" + description + '}';
    }
}
