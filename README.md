# Java Package operator

Explain the purpose of this project

- Build the project and publish the image on a registry
```shell
mvn clean package \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.image=quay.io/halkyonio/java-package-operator:0.1.0-SNAPSHOT
```
- Deploy the kube manifest on a kube cluster and install the CRD
```shell
k delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes/kubernetes.yml
k delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes/packages.io.halkyon-v1.yml

k apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes/kubernetes.yml
k apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes/packages.io.halkyon-v1.yml
```
- Install an example
```shell
k delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/examples/package1.yml
k apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/examples/package1.yml
```

