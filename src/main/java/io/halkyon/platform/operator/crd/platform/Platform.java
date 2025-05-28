package io.halkyon.platform.operator.crd.platform;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;

@Group("halkyon.io")
@Version("v1alpha1")
@ShortNames("pkg")
// @Kind("Platform")
// @Plural("platforms")
// @Singular("platform")
public class Platform extends CustomResource<PlatformSpec, PlatformStatus> implements Namespaced {
    @Override
    public String toString() {
        return "Platform{" +
            "apiVersion='" + getApiVersion() + '\'' +
            ", kind='" + getKind() + '\'' +
            ", metadata=" + getMetadata() +
            ", spec=" + getSpec() +
            ", status=" + getStatus() +
            '}';
    }
}
