# Java Package operator

TODO: Explain the purpose of this project

- Build the project and publish the image on a registry
```shell
mvn clean package \
  -Dquarkus.kubernetes.rbac.service-accounts.package-operator.namespace=default \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.image=quay.io/halkyonio/java-package-operator:0.1.0-SNAPSHOT
```
- Deploy the kube manifest on a kube cluster and install the CRD
```shell
k delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/examples/package1.yml
k delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes//kubernetes.yml
k delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes/packages.halkyon.io-v1.yml

k apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes/packages.halkyon.io-v1.yml
k apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/target/kubernetes/kubernetes.yml
```
- Install a `Package`:
```shell
k apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/examples/package1.yml

k get package/my-helm-app -oyaml
apiVersion: halkyon.io/v1
kind: Package
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"halkyon.io/v1","kind":"Package","metadata":{"annotations":{},"name":"my-helm-app"},"spec":{"tool":"Helm","url":"https://charts.helm.sh/stable/nginx-ingress-1.41.3.tgz","version":"1.41.3"}}
  creationTimestamp: "2025-05-22T15:05:30Z"
  generation: 1
  name: my-helm-app
  resourceVersion: "67780"
  uid: cb62ebeb-0177-4ccf-8f52-33bf9fbec145
spec:
  tool: Helm
  url: https://charts.helm.sh/stable/nginx-ingress-1.41.3.tgz
  version: 1.41.3
```

