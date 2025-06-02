package io.halkyon.platform.operator.resources;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.halkyon.platform.operator.controller.PlatformReconciler;
import io.halkyon.platform.operator.crd.Platform;
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
public class ConfigMapDR extends CRUDKubernetesDependentResource<ConfigMap, Platform> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    public ConfigMapDR() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(Platform platform, Context<Platform> ctx) {
        LOG.info("Processing the platform resource: {} for the dependent resource: ConfigMap", platform.getMetadata().getName());

        if (platform.getSpec().getVersion().equals("0.1.1")) {
            LOG.info("Platform has been updated. Version is now: {}", platform.getSpec().getVersion());
        } else {
            LOG.info("Original platform version is {}", platform.getSpec().getVersion());
        }

        platform.setStatus(createStatus("ConfigMap created"));

        Map<String, String> data = new HashMap<>();
        data.put("version", platform.getSpec().getVersion());
        Map<String, String> labels = new HashMap<>();
        labels.put(SELECTOR, "true");
        return new ConfigMapBuilder()
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName(configMapName(platform))
                    .withNamespace(platform.getMetadata().getNamespace())
                    .withLabels(labels)
                    .build())
            .withData(data)
            .build();
    }
}
