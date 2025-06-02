package io.halkyon.platform.operator.resources;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.halkyon.platform.operator.crd.Package;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KubernetesDependent()
public class PodDR extends CRUDKubernetesDependentResource<Pod, Package> {
    private final static Logger LOG = LoggerFactory.getLogger(PodDR.class);

    public PodDR() {
        super(Pod.class);
    }

    @Override
    protected Pod desired(Package pkg, Context<Package> ctx) {
        Pod podPkg = new PodBuilder()
            .withNewMetadata()
              .withName(pkg.getMetadata().getName())
            .endMetadata()
            .build();
        return podPkg;
    }
}
