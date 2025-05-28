package io.halkyon.platform.operator.resources;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.halkyon.platform.operator.crd.platform.Platform;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

import java.util.HashMap;
import java.util.Map;

import static io.halkyon.platform.operator.Utils.configMapName;
import static io.halkyon.platform.operator.controller.PlatformReconciler.SELECTOR;

// this annotation only activates when using managed dependents and is not otherwise needed
@KubernetesDependent(informer = @Informer(labelSelector = SELECTOR))
public class ConfigMapDependentResource extends CRUDKubernetesDependentResource<ConfigMap, Platform> {

  @Override
  protected ConfigMap desired(Platform platform, Context<Platform> context) {
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
