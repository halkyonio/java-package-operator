package io.halkyon.platform.operator.model;

import io.smallrye.config.WithDefault;

public class Namespace {
    /**
     * The target name of the namespace where the resources should be deployed
     */
    @WithDefault("default")
    private String name;

    /**
     * Boolean indicating if the namespace should be created by the tool: helm, kubectl, etc
     */
    @WithDefault("true")
    private boolean createNamespace;

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
