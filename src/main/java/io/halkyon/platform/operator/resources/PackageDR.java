package io.halkyon.platform.operator.resources;

import io.halkyon.platform.operator.crd.Package;
import io.halkyon.platform.operator.crd.PackageStatus;
import io.halkyon.platform.operator.crd.Platform;
import io.halkyon.platform.operator.model.PackageDefinition;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.halkyon.platform.operator.model.Status.READY;

// this annotation only activates when using managed dependents and is not otherwise needed
@KubernetesDependent()
public class PackageDR extends CRUDKubernetesDependentResource<Package, Platform> {
    private final static Logger LOG = LoggerFactory.getLogger(PackageDR.class);

    public PackageDR() {
        super(Package.class);
    }

    @Override
    protected Package desired(Platform platform, Context<Platform> ctx) {
        LOG.info("Processing the platform resource: {} for the dependent resource: Package", platform.getMetadata().getName());

        // Creating the Package to be processed using the packages list defined part of the Platform CR spec
        PackageDefinition pkgModel = platform.getSpec().getPackages().getFirst();
        pkgModel.setStatus(String.valueOf(READY));
        Package pkg = new Package();
        pkg.getMetadata().setName(pkgModel.getName());
        pkg.getMetadata().setNamespace(platform.getMetadata().getNamespace());
        pkg.setStatus(new PackageStatus().withMessage(pkgModel.getStatus()));
        return pkg;
    }
}
