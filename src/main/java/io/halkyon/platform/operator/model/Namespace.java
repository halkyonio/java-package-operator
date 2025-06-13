package io.halkyon.platform.operator.model;

public class Namespace {
    /**
     * The target name of the namespace where the resources should be deployed
     */
    private String name = "default";

    /**
     * Boolean indicating if the namespace should be created by the tool: helm, kubectl, etc
     */
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
