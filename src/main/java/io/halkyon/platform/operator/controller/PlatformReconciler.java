package io.halkyon.platform.operator.controller;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.halkyon.platform.operator.crd.pkg.Package;
import io.halkyon.platform.operator.crd.platform.Platform;
import io.halkyon.platform.operator.crd.platform.PlatformStatus;
import io.halkyon.platform.operator.resources.ConfigMapDependentResource;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Workflow(
    dependents = {
        @Dependent(type = ConfigMapDependentResource.class),
    })
public class PlatformReconciler implements Reconciler<Platform> {
    private final static Logger log = LoggerFactory.getLogger(PlatformReconciler.class);

    public static final String SELECTOR = "managed";

    public UpdateControl<Platform> reconcile(Platform platform, Context<Platform> context) {
        final var name =
            context.getSecondaryResource(ConfigMap.class).orElseThrow().getMetadata().getName();
        return UpdateControl.patchStatus(createPlatformForStatusUpdate(platform, name));
    }

    public static Platform createPlatformForStatusUpdate(Platform platform, String configMapName) {
        Platform res = new Platform();
        res.setMetadata(
            new ObjectMetaBuilder()
                .withName(platform.getMetadata().getName())
                .withNamespace(platform.getMetadata().getNamespace())
                .build());
        res.setStatus(createStatus(configMapName));
        return res;
    }

    public static PlatformStatus createStatus(String configMapName) {
        PlatformStatus status = new PlatformStatus();
        status.setMessage(configMapName);
        return status;
    }
}