package io.halkyon.crd;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.halkyon.crd.pkg.Package;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.Map;

public class ConfigMapDR extends CRUDKubernetesDependentResource<ConfigMap, io.halkyon.crd.pkg.Package> {

    public ConfigMapDR() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(io.halkyon.crd.pkg.Package aPackage, Context<Package> context) {
        return new ConfigMapBuilder()
            .withNewMetadata()
            .withName(aPackage.getMetadata().getName() + "-config")
            .withNamespace(aPackage.getMetadata().getNamespace())
            .endMetadata()
            .withData(Map.of("tool", aPackage.getSpec().getTool()))
            .build();
    }
}
