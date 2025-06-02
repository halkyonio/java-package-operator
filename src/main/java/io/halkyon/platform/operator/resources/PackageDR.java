package io.halkyon.platform.operator.resources;

import io.halkyon.platform.operator.PackageUtils;
import io.halkyon.platform.operator.crd.PackageCR;
import io.halkyon.platform.operator.crd.PlatformCR;
import io.halkyon.platform.operator.model.Package;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

// this annotation only activates when using managed dependents and is not otherwise needed
@KubernetesDependent()
public class PackageDR extends CRUDKubernetesDependentResource<PackageCR, PlatformCR> {
    private final static Logger LOG = LoggerFactory.getLogger(PackageDR.class);

    public PackageDR() {
        super(PackageCR.class);
    }

    @Override
    protected PackageCR desired(PlatformCR platformCR, Context<PlatformCR> ctx) {
        LOG.info("Processing the platform resource: {} for the dependent resource: Package", platformCR.getMetadata().getName());
        return null;
    }
}
