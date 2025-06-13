package io.halkyon.platform.operator.model;

import io.fabric8.generator.annotation.Default;

public class Namespace {
    /**
     * The target name of the namespace where the resources should be deployed
     */
    @Default("default")
    private String name = "default";

    /**
     * Boolean indicating if the namespace should be created by the tool: helm, kubectl, etc
     */
    @Default("true")
    private boolean createNamespace = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getCreateNamespace() {
        return createNamespace;
    }

    public void setCreateNamespace(boolean created) {
        this.createNamespace = created;
    }
}
