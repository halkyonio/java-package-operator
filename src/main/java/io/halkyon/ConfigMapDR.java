package io.halkyon;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.Map;

public class ConfigMapDR extends CRUDKubernetesDependentResource<ConfigMap, Package> {

    public ConfigMapDR() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(Package aPackage, Context<Package> context) {
        return new ConfigMapBuilder()
            .withNewMetadata()
            .withName(aPackage.getMetadata().getName() + "-config")
            .withNamespace(aPackage.getMetadata().getNamespace())
            .endMetadata()
            .withData(Map.of("tool", aPackage.getSpec().getTool()))
            .build();
    }
}
