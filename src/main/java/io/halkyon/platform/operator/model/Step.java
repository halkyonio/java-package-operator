package io.halkyon.platform.operator.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.crd.generator.annotation.PreserveUnknownFields;
import io.fabric8.generator.annotation.Required;

public class Step {
    @Required
    private String name;
    @Required
    private String image;

    private String description;
    private String id;
    private String script;
    private Helm helm;
    private WaitCondition waitCondition;
    private Namespace namespace;

    @PreserveUnknownFields
    @JsonPropertyDescription("Values specifies the Helm values to be passed to the helm template command and defined as a map.")
    private String values;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public Helm getHelm() {
        return helm;
    }

    public void setHelm(Helm helm) {
        this.helm = helm;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public WaitCondition getWaitCondition() {
        return waitCondition;
    }

    public void setWaitCondition(WaitCondition waitCondition) {
        this.waitCondition = waitCondition;
    }

}
