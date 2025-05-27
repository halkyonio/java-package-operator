# Java Package operator

TODO: Explain the purpose of this project

- Build the project and publish the image on a registry where you have write access (example: quay.io)
```shell
mvn clean package \
  -Dquarkus.kubernetes.output-directory=resources/manifests \
  -Dquarkus.operator-sdk.crd.output-directory=resources/crds \
  -Dquarkus.operator-sdk.crd.generate-all \
  -Dquarkus.kubernetes.rbac.service-accounts.package-operator.namespace=default \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.image=quay.io/halkyonio/java-package-operator:0.1.0-SNAPSHOT
```
- Deploy the kube manifest on a kube cluster and install the CRD
```shell
kubectl delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/examples/package1.yml
kubectl delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/manifests/kubernetes.yml
kubectl delete -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/crds/packages.halkyon.io-v1.yml

kubectl apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/crds/packages.halkyon.io-v1.yml
kubectl apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/manifests/kubernetes.yml
```
- Create a `Package` yaml
```yaml
apiVersion: halkyon.io/v1
kind: Package
metadata:
  name: my-helm-app
  namespace: default
spec:
  tool: Helm
  url: https://charts.helm.sh/stable/nginx-ingress-1.41.3.tgz
  version: 1.41.3
```
- and deploy it: 
```shell
kubectl apply -f ~/code/halkyonio/java-kind-cli-and-operator/package-operator/resources/examples/package1.yml
```

- Verify that the status has changed:
```shell
kubectl get package/my-helm-app -oyaml
apiVersion: halkyon.io/v1
kind: Package
metadata:
...
  generation: 1
  name: my-helm-app
spec:
  tool: Helm
  url: https://charts.helm.sh/stable/nginx-ingress-1.41.3.tgz
  version: 1.41.3
status:
  message: my-helm-app  # Status added by the controller !
````  
- A configMap has been created by the controller and is owned by the `Package` CR :-)
```shell
kubectl get cm/my-helm-app-config -oyaml
apiVersion: v1
data:
  tool: Helm
kind: ConfigMap
metadata:
  annotations:
    javaoperatorsdk.io/previous: f1a062d2-94b4-4ba4-842d-e99f90a3ed74
  name: my-helm-app-config
  namespace: default
  ownerReferences:
  - apiVersion: halkyon.io/v1
    kind: Package
    name: my-helm-app
    uid: e2872b3f-ea9b-4803-9b3f-30e708027a81  
```

## Work in progress

Use the Glue Operator to manage using a workflow the `pre-install`, `post-install` and `installation` steps as jobs

Install Glue on an existing kind cluster
```shell
kubectl apply -f https://github.com/java-operator-sdk/kubernetes-glue-operator/releases/latest/download/glues.io.javaoperatorsdk.operator.glue-v1.yml -f https://github.com/java-operator-sdk/kubernetes-glue-operator/releases/latest/download/glueoperators.io.javaoperatorsdk.operator.glue-v1.yml
kubectl apply -f https://github.com/java-operator-sdk/kubernetes-glue-operator/releases/latest/download/kubernetes.yml
```

Deploy our CRDs and Custom resource object of a simple workflow
```shell
k rollout status deployment kubernetes-glue-operator --timeout=90s

k delete -f resources/examples/glue/simple-platform.yml
k delete -f resources/examples/glue/crds/platforms.halkyon.io-v1alpha1.yml
k delete -f resources/examples/glue/crds/platform-operator.yml

k apply -f resources/examples/glue/crds/platforms.halkyon.io-v1alpha1.yml
k apply -f resources/examples/glue/crds/platform-operator.yml
k apply -f resources/examples/glue/simple-platform.yml

k logs -lapp.kubernetes.io/name=kubernetes-glue-operator --tail=-1
```

You can also as alternative, launch the Main class of the Glue Operator within your IDE or terminal
```shell
// io.javaoperatorsdk.operator.glue.Main
git clone https://github.com/java-operator-sdk/kubernetes-glue-operator.git; cd kubernetes-glue-operator

export KUBECONFIG=<PATH_TO_KUBECONFIG> or set -x KUBECONFIG <PATH_TO_KUBECONFIG>
mvn clean package -DskipTests
java -jar target/kubernetes-glue-operator-0.10.1-SNAPSHOT-runner.jar
```
