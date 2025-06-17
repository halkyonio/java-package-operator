package io.halkyon.platform.operator;

public enum Mode {
    WAIT_FOR,
    HELM_INSTALL,
    HELM_UNINSTALL,
    MANIFEST_INSTALL,
    MANIFEST_UNINSTALL,
    SCRIPT_INSTALL,
    SCRIPT_UNINSTALL,
}
