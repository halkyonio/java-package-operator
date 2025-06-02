package io.halkyon.platform.operator.controller;

import io.halkyon.platform.operator.crd.PackageCR;
import io.halkyon.platform.operator.crd.PlatformCR;
import io.halkyon.platform.operator.crd.PlatformStatus;
import io.halkyon.platform.operator.resources.PackageDR;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Workflow(
    dependents = {
        @Dependent(type = PackageDR.class),
    })
public class PlatformReconciler implements Reconciler<PlatformCR>, Cleaner<PlatformCR> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public static final String SELECTOR = "managed";

    public UpdateControl<PlatformCR> reconcile(PlatformCR platformCR, Context<PlatformCR> context) {
        LOG.info("Reconciling platform {}", platformCR.getMetadata().getName());
        final var name =
            context.getSecondaryResource(PackageCR.class).orElseThrow().getMetadata().getName();

         if (platformCR.getStatus().getMessage().equals("ConfigMap created")) {
             LOG.info("Platform status updated.");
             String newStatus = platformCR.getStatus().getMessage().concat("\nStatus updated");
             PlatformStatus platformStatus = new PlatformStatus();
             platformStatus.setMessage(newStatus);
             platformCR.setStatus(platformStatus);
         }
        return UpdateControl.patchStatus(platformCR);
    }

    public static PlatformStatus createStatus(String configMapName) {
        PlatformStatus status = new PlatformStatus();
        status.setMessage(configMapName);
        return status;
    }

    @Override
    public DeleteControl cleanup(PlatformCR platformCR, Context<PlatformCR> context) throws Exception {
        LOG.info("Platform resource: {} deleted", platformCR.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }
}