package io.halkyon.platform.operator.controller;

import io.halkyon.platform.operator.PackageUtils;
import io.halkyon.platform.operator.crd.PackageCR;
import io.halkyon.platform.operator.crd.PlatformCR;
import io.halkyon.platform.operator.crd.PlatformStatus;
import io.halkyon.platform.operator.model.Package;
import io.halkyon.platform.operator.model.Platform;
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
public class PlatformReconciler implements Reconciler<PlatformCR>, Cleaner<PlatformCR> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public static final String SELECTOR = "managed";

    public UpdateControl<PlatformCR> reconcile(PlatformCR platformCR, Context<PlatformCR> context) {
        var name = platformCR.getMetadata().getName();
        LOG.info("Reconciling platform {}", name);

        if (!platformCR.getSpec().getPackages().isEmpty()) {
            LinkedList<Package> pkgs = PackageUtils.orderPackages(platformCR.getSpec().getPackages());
            PlatformStatus pStatus = new PlatformStatus();
            pStatus.setPackages(pkgs);

            platformCR.setStatus(pStatus);

            return UpdateControl.patchStatus(platformCR);
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
    public DeleteControl cleanup(PlatformCR platformCR, Context<PlatformCR> context) throws Exception {
        LOG.info("Platform resource: {} deleted", platformCR.getMetadata().getName());
        return DeleteControl.defaultDelete();
    }
}