package io.halkyon;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Workflow(dependents = @Dependent(type = ConfigMapDR.class))
public class PackageReconciler implements Reconciler<Package> {
    private final static Logger log = LoggerFactory.getLogger(PackageReconciler.class);
    @Override
    public UpdateControl<Package> reconcile(Package resource, Context<Package> context) {
        resource.setStatus(new PackageStatus().withMessage(resource.getMetadata().getName()));
        log.info("Set the status of the package resource: {}, status: {}",resource.getMetadata().getName(),resource.getStatus().getMessage());
        return UpdateControl.patchStatus(resource);
    }
}