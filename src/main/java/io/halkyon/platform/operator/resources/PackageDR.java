package io.halkyon.platform.operator.resources;

import io.halkyon.platform.operator.controller.PlatformReconciler;
import io.halkyon.platform.operator.crd.PackageCR;
import io.halkyon.platform.operator.crd.PlatformCR;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.halkyon.platform.operator.controller.PlatformReconciler.createStatus;

// this annotation only activates when using managed dependents and is not otherwise needed
@KubernetesDependent()
public class PackageDR extends CRUDKubernetesDependentResource<PackageCR, PlatformCR> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public PackageDR() {
        super(PackageCR.class);
    }

    @Override
    protected PackageCR desired(PlatformCR platformCR, Context<PlatformCR> ctx) {
        LOG.info("Processing the platform resource: {} for the dependent resource: Package", platformCR.getMetadata().getName());

        platformCR.setStatus(createStatus(String.format("Processing the package: %s", platformCR.getMetadata().getName())));

        final PackageCR aPackageCR = new PackageCR();;
        platformCR.getSpec().getPackages().stream().findFirst().ifPresent(pkg -> {
            aPackageCR.getMetadata().setName(pkg.getMetadata().getName());
            aPackageCR.getSpec().setTool(pkg.getSpec().getTool());
            aPackageCR.getSpec().setRepoUrl(pkg.getSpec().getRepoUrl());
        });
        return aPackageCR;
    }
}
