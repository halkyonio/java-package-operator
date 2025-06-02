package io.halkyon.platform.operator.controller;

import io.halkyon.platform.operator.PackageUtils;
import io.halkyon.platform.operator.model.Package;
import io.halkyon.platform.operator.crd.Platform;
import io.halkyon.platform.operator.crd.PlatformStatus;
import io.halkyon.platform.operator.resources.PackageDR;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

@Workflow(
    dependents = {
        @Dependent(type = PackageDR.class),
    })
public class PlatformReconciler implements Reconciler<Platform>, Cleaner<Platform> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public static final String SELECTOR = "managed";

    public UpdateControl<Platform> reconcile(Platform platform, Context<Platform> context) {
        var name = platform.getMetadata().getName();
        LOG.info("Reconciling platform {}", name);

        if (!platform.getSpec().getPackages().isEmpty()) {
            LinkedList<Package> pkgs = PackageUtils.orderPackages(platform.getSpec().getPackages());
            PlatformStatus pStatus = new PlatformStatus();
            pStatus.setPackages(pkgs);

            platform.setStatus(pStatus);

            return UpdateControl.patchStatus(platform);
        } else {
            LOG.warn("No managed packages found");
            return null;
        }

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