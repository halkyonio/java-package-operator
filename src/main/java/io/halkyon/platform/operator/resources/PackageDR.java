package io.halkyon.platform.operator.resources;

import io.halkyon.platform.operator.controller.PlatformReconciler;
import io.halkyon.platform.operator.crd.platform.Platform;
import io.halkyon.platform.operator.crd.platform.Package;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.halkyon.platform.operator.controller.PlatformReconciler.SELECTOR;
import static io.halkyon.platform.operator.controller.PlatformReconciler.createStatus;

// this annotation only activates when using managed dependents and is not otherwise needed
@KubernetesDependent()
public class PackageDR extends CRUDKubernetesDependentResource<Package, Platform> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public PackageDR() {
        super(Package.class);
    }

    @Override
    protected Package desired(Platform platform, Context<Platform> ctx) {
        LOG.info("Processing the platform resource: {} for the dependent resource: Package", platform.getMetadata().getName());

        if (platform.getSpec().getVersion().equals("0.1.1")) {
            LOG.info("Platform has been updated. Version is now: {}", platform.getSpec().getVersion());
        } else {
            LOG.info("Original platform version is {}", platform.getSpec().getVersion());
        }

        platform.setStatus(createStatus("Package created"));

        Map<String, String> data = new HashMap<>();
        data.put("version", platform.getSpec().getVersion());

        Map<String, String> labels = new HashMap<>();
        labels.put(SELECTOR, "true");

        Package aPackage = new Package();
        aPackage.getSpec().setTool("helm");
        aPackage.getSpec().setUrl("bitnami.io/helm/ingress");
        return aPackage;
    }
}
