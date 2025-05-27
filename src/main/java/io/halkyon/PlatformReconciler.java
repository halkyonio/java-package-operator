package io.halkyon;

import io.halkyon.crd.platform.Platform;
import io.halkyon.crd.platform.PlatformStatus;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformReconciler {
    private final static Logger log = LoggerFactory.getLogger(PlatformReconciler.class);

    public UpdateControl<Platform> reconcile(Platform resource, Context<Platform> context) {
        resource.setStatus(new PlatformStatus().withMessage(resource.getMetadata().getName()));
        log.info("Set the status of the platform resource: {}, status: {}",resource.getMetadata().getName(),resource.getStatus().getMessage());
        return UpdateControl.patchStatus(resource);
    }
}