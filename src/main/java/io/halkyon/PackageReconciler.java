package io.halkyon;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;

@Workflow(dependents = @Dependent(type = ConfigMapDR.class))
public class PackageReconciler implements Reconciler<Package> {
    @Override
    public UpdateControl<Package> reconcile(Package resource, Context<Package> context) {
        resource.setStatus(new PackageStatus().withMessage(resource.getMetadata().getName()));
        return UpdateControl.patchStatus(resource);
    }
}