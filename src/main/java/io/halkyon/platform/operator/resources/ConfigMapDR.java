package io.halkyon.platform.operator.resources;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.halkyon.platform.operator.controller.PlatformReconciler;
import io.halkyon.platform.operator.crd.platform.PlatformCR;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.halkyon.platform.operator.Utils.configMapName;
import static io.halkyon.platform.operator.controller.PlatformReconciler.SELECTOR;
import static io.halkyon.platform.operator.controller.PlatformReconciler.createStatus;

// this annotation only activates when using managed dependents and is not otherwise needed
@KubernetesDependent(informer = @Informer(labelSelector = SELECTOR))
public class ConfigMapDR extends CRUDKubernetesDependentResource<ConfigMap, PlatformCR> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public ConfigMapDR() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(PlatformCR platformCR, Context<PlatformCR> ctx) {
        LOG.info("Processing the platform resource: {} for the dependent resource: ConfigMap", platformCR.getMetadata().getName());

        if (platformCR.getSpec().getVersion().equals("0.1.1")) {
            LOG.info("Platform has been updated. Version is now: {}", platformCR.getSpec().getVersion());
        } else {
            LOG.info("Original platform version is {}", platformCR.getSpec().getVersion());
        }

        platformCR.setStatus(createStatus("ConfigMap created"));

        Map<String, String> data = new HashMap<>();
        data.put("version", platformCR.getSpec().getVersion());
        Map<String, String> labels = new HashMap<>();
        labels.put(SELECTOR, "true");
        return new ConfigMapBuilder()
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName(configMapName(platformCR))
                    .withNamespace(platformCR.getMetadata().getNamespace())
                    .withLabels(labels)
                    .build())
            .withData(data)
            .build();
    }
}
