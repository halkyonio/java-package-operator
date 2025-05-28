package io.halkyon.platform.operator.controller;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.halkyon.platform.operator.crd.platform.Package;
import io.halkyon.platform.operator.crd.platform.Platform;
import io.halkyon.platform.operator.crd.platform.PlatformStatus;
import io.halkyon.platform.operator.resources.ConfigMapDR;
import io.halkyon.platform.operator.resources.PackageDR;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Workflow(
    dependents = {
        @Dependent(type = PackageDR.class),
    })
public class PlatformReconciler implements Reconciler<Platform>, Cleaner<Platform> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public static final String SELECTOR = "managed";

    public UpdateControl<Platform> reconcile(Platform platform, Context<Platform> context) {
        LOG.info("Reconciling platform {}", platform.getMetadata().getName());
        final var name =
            context.getSecondaryResource(Package.class).orElseThrow().getMetadata().getName();

         if (platform.getStatus().getMessage().equals("ConfigMap created")) {
             LOG.info("Platform status updated.");
             String newStatus = platform.getStatus().getMessage().concat("\nStatus updated");
             PlatformStatus platformStatus = new PlatformStatus();
             platformStatus.setMessage(newStatus);
             platform.setStatus(platformStatus);
         }

        return UpdateControl.patchStatus(platform);
    }

    public static PlatformStatus createStatus(String configMapName) {
        PlatformStatus status = new PlatformStatus();
        status.setMessage(configMapName);
        return status;
    }

    @Override
    public DeleteControl cleanup(Platform platform, Context<Platform> context) throws Exception {
        LOG.info("Platform resource: {} deleted", platform.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }
}