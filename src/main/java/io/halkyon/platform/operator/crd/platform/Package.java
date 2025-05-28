package io.halkyon.platform.operator.crd.platform;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("halkyon.io")
@Version("v1alpha1")
public class Package extends CustomResource<PackageSpec, PackageStatus> implements Namespaced {

    @Override
    public String toString() {
        return "Package{" +
            "apiVersion='" + getApiVersion() + '\'' +
            ", kind='" + getKind() + '\'' +
            ", metadata=" + getMetadata() +
            ", spec=" + getSpec() +
            ", status=" + getStatus() +
            '}';
    }
}
