# Java Platform Kubernetes operator

The Java Platform Kubernetes operator projects aims to provision a cluster without the pain to have to deal with scripts locally (bash, python, etc), to manage the dependencies or to wait till services, resources (deployment, pod, ...) are up and running.

With the help of our `Platform Custom Resource`, you can declare using the section `Packages` what you would like to install (ingress, backstage, gitea, argocd, etc) and the `How` is defined part of a simple Pipeline composed of steps able to perform: init, install, uninstall or something post-installation.

This project is still a WIP and additional features, improvements will come such as:

- Support to mount script(s) to be executed part of pipeline step(s)
- Implement a simple mechanism to start a container when the previous finished (mounted volume with shared files, etc)

## Rational of this project

While several projects already exist to install and sync resources: Argocd, Kro, Crossplane, ... most of them don't support to define easily steps to be executed, before or after a package is installed, rely heavily on user's scripts to define the scenario to be executed when x packages are installed able to verify if pod, deployment, service are ready, if a health check endpoint returns an HTTP 200 response, etc and don't support to declare the dependencies between the packages/applications: https://github.com/argoproj/argo-cd/issues/7437

TODO: To be continued

## How to play with it

Install locally a Kubernetes cluster running our `Platform controller` using the [Java Kind client](https://github.com/halkyonio/java-kind-client)

Create next a `Platform` definition [file](resources/examples/platform-ingress-gitea.yml) packaging by example: `ingress` and `gitea`.
For that purpose, you will have to create a custom resource file having the following structure:

```yaml
apiVersion: halkyon.io/v1alpha1
kind: Platform
metadata:
  name: ingress-gitea
...  
spec:
  packages:
```
For each package, you will declare a pipeline including steps: `init` (optional) or `install`. 

**Note**: Until now it is mandatory to adopt this naming convention of the steps as the controller is searching about them !

The step `install` will be used to perform a task which can be declared part of the `script` field or using tools such as: `helm`, `kubectl` configured respectively with the fields: `helm` and `file`

```yaml
apiVersion: halkyon.io/v1alpha1
kind: Platform
metadata:
  name: ingress-gitea
...  
spec:
  packages:
    - name: nginx-ingress
      description: "nginx-ingress package"
      pipeline:
        steps:
          - name: install
            image: dtzar/helm-kubectl
            helm:
              chart:
```
The target namespace where the resources should be deployed is defined using the field: `namespace`. If not specifield, they will be deployed under the `default` namespace.

```yaml
apiVersion: halkyon.io/v1alpha1
kind: Platform
metadata:
  name: ingress-gitea
...  
spec:
  packages:
    - name: nginx-ingress
      description: "nginx-ingress package"
      pipeline:
        steps:
          - name: install
            image: dtzar/helm-kubectl
            namespace:
              name: ingress
            helm:
              chart:
```

For helm, you will configure the parameters such as the url of the chart repository (= index.yaml), its version and the name of the chart to be fetched from the repository and to be deployed. The helm values will be defined using the `values` field.

```yaml
apiVersion: halkyon.io/v1alpha1
kind: Platform
metadata:
  name: ingress-gitea
  namespace: platform
spec:
  version: 0.1.0
  description: "A platform example installing the following packages nginx ingress and gitea and exposing the gitea ui at the address https://gitea.localtest.me:8443"
  packages:
    - name: nginx-ingress
      description: "nginx-ingress package"
      pipeline:
        steps:
          - name: install
            image: dtzar/helm-kubectl
            namespace:
              name: ingress
            helm:
              chart:
                repoUrl: https://kubernetes.github.io/ingress-nginx
                name: ingress-nginx
                version: 4.12.2
              values: |
                controller:
                  hostPort:
                    enabled: true
                  service:
                    type: NodePort
                ingress:
                  enabled: true
```

When it is needed to process to a step before to install a package, we will use the `init` step. Until now, this step content will be used to configure a pod's init container. The `init` step can be used to generate a configMap, a kubernetes resource not packaged part of the manifest or as helm chart template but can also be used to wait till a service, endpoint is ready, healthy as we will do with the package gitea

```yaml
    - name: gitea
      description: "gitea package"
      pipeline:
        steps:
          - name: init
            image: dtzar/helm-kubectl
            namespace:
              name: default
            waitCondition:
              type: service
              endpoint:
                name: ingress-nginx-controller-admission
                port: 443
              path: /health

          - name: install
            image: dtzar/helm-kubectl
            namespace:
              name: gitea
            helm:
              chart:
                repoUrl: https://dl.gitea.com/charts/
                name: gitea
              values: |
                redis-cluster:
                  enabled: false
                postgresql:
                  enabled: false
                postgresql-ha:
                  enabled: false
                valkey-cluster:
                  enabled: false
                persistence:
                  enabled: false
                gitea:
                  admin:
                    # existingSecret: <NAME_OF_SECRET>
                    username: "giteaAdmin"
                    password: "developer"
                    email: "gi@tea.com"
                  config:
                    database:
                      DB_TYPE: sqlite3
                    session:
                      PROVIDER: memory
                    cache:
                      ADAPTER: memory
                    queue:
                      TYPE: level
                service:
                  ssh:
                    type: NodePort
                    nodePort: 32222
                    externalTrafficPolicy: Local
                ingress:
                  enabled: true
                  className: nginx
                  hosts:
                    - host: gitea.localtest.me
                      paths:
                        - path: /
                          pathType: Prefix
```

Deploy now the platform yaml file using the example file: 
```shell
kubectl apply -f resources/examples/platform-ingress-gitea.yml
```

Check the platform resource and status
```shell
kubectl get platform/ingress-gitea -n platform
NAME            AGE
ingress-gitea   25s

kubectl get platform/ingress-gitea -n platform -ojson | jq -r '.status.conditions'
[
  {
    "message": "Deploying the package: nginx-ingress",
    "type": "Deploying"
  },
  {
    "message": "Deploying the package: nginx-ingress",
    "type": "Deploying"
  },
  {
    "message": "Deploying the package: nginx-ingress",
    "type": "Deploying"
  },
  {
    "message": "Deploying the package: gitea",
    "type": "Deploying"
  },
  {
    "message": "Deploying the package: gitea",
    "type": "Deploying"
  }
]
```

When done, access the gitea url: `https://gitea.localtest.me:8443` and log on using as username/password: `giteaAdmin` and `developer`

**Note**: To clean up the cluster, simply delete the platform resource file installed
```shell
kubectl delete -f resources/examples/platform-ingress-gitea.yml
```

Enjoy ;-)

## For the developers

Build the project and publish the image on a registry where you have write access (example: quay.io)
```shell
mvn clean package \
  -Dquarkus.kubernetes.output-directory=resources/manifests \
  -Dquarkus.operator-sdk.crd.output-directory=resources/crds \
  -Dquarkus.operator-sdk.crd.generate-all \
  -Dquarkus.kubernetes.rbac.service-accounts.package-operator.namespace=platform \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.image=quay.io/halkyonio/java-package-operator:0.1.0-SNAPSHOT
```
When it is only needed to build/push a new image
```shell
mvn clean package -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.image=quay.io/halkyonio/java-package-operator:0.1.0-SNAPSHOT
```
and to regenerate new CRDs
```shell
mvn clean package -Dquarkus.operator-sdk.crd.output-directory=resources/crds \
  -Dquarkus.operator-sdk.crd.generate-all
```

## Work in progress

Use the [Glue Operator](https://github.com/java-operator-sdk/kubernetes-glue-operator) to manage using a workflow the `pre-install`, `post-install` and `installation` steps as jobs

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
