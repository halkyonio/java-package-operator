# Java Platform Kubernetes operator

The Java Platform Kubernetes operator projects aims to provision a cluster without the pain to have to deal with scripts locally (bash, python, etc), to manage the depends on or to wait till services, resources (deployment, pod, ...) are up and running.

With the help of our `Platform Custom Resource`, you can declare using the section `Packages` what you would like to install (ingress, backstage, gitea, argocd, etc) and the `How` is defined part of a simple Pipeline composed of steps able to perform: init, install, uninstall or do something post-installation.

This project is still a WIP and additional features, improvements will come such as:

- Add `step` field to specify the tool to be used: helm, file, kustomize, etc and their parameters or values to be passed to Helm
- Support to mount script(s) to be executed part of pipeline step(s)
- Delete the pod used to `uninstall` a package when the Package CR is deleted
- Implement a simple mechanism to start a container when the previous finished (mounted volume with shared files, etc)

## Rational of this project

While several projects already exist to install and sync resources: Argocd, Kro, Crossplane, ... most of them don't support to define easily steps to be executed, before or after a package is installed, rely on user's scripts to define the scenario to be executed when x packages are installed able to verify if pod, deployment, service are ready, if a health check endpoint returns a HTTP 200 response, etc and don't support to declare the dependencies between the packages/applications: https://github.com/argoproj/argo-cd/issues/7437

TODO


## How to play with it

Install locally a Kubernetes cluster running the `Platform controller` using the [Java Kind client](https://github.com/halkyonio/java-kind-client)

Create next a `Platform` definition file packaging by example: `ingress` and `gitea`
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
          - name: install-nginx-ingress
            image: dtzar/helm-kubectl
            repoUrl: https://kubernetes.github.io/ingress-nginx
            version: 4.12.2
            script: |
              cat << EOF > values.yml
              controller:
                hostPort:
                  enabled: true
                service:
                  type: NodePort
              ingress:
                enabled: true
              EOF
              helm repo add ingress https://kubernetes.github.io/ingress-nginx
              helm repo update
              helm uninstall nginx-ingress -n ingress || echo "Release 'nginx-ingress' not found. Continuing..."
              helm install nginx-ingress ingress/ingress-nginx \
                 --version 4.12.2 \
                 --namespace ingress \
                 --create-namespace \
                 -f values.yml
              echo "This is installation step"
            values: |
              ingress:
                enabled: true
          - name: post-install
            image: dtzar/helm-kubectl
            script: |
              echo "WaitFor Ingress"
              # kubectl rollout status deployment nginx-ingress-ingress-nginx-controller -n ingress --timeout=90s
              until curl -s http://nginx-ingress-ingress-nginx-controller-admission.ingress.svc.cluster.local:443/healthz; do
                echo "Waiting for service to be ready..."
                sleep 5
              done
              echo "Ingress Webhook Service is ready!"
          - name: uninstall
            image: dtzar/helm-kubectl
            script: |
              helm uninstall nginx-ingress -n ingress

    - name: gitea
      description: "gitea package"
      pipeline:
        steps:
          - name: init
            image: dtzar/helm-kubectl
            script: |
              echo "WaitFor Ingress"
              # kubectl rollout status deployment nginx-ingress-ingress-nginx-controller -n ingress --timeout=90s
              until curl -s http://nginx-ingress-ingress-nginx-controller-admission.ingress.svc.cluster.local:443/healthz; do
                echo "Waiting for service to be ready..."
                sleep 5
              done
          - name: install
            image: dtzar/helm-kubectl
            script: |
              cat << EOF > values.yml
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
              EOF
              helm repo add gitea-charts https://dl.gitea.com/charts/
              helm repo update
              helm uninstall gitea -n gitea || echo "Release 'gitea' not found. Continuing..."
              helm install gitea gitea-charts/gitea \
                -n gitea \
                --create-namespace \
                -f values.yml

          - name: uninstall
            image: dtzar/helm-kubectl
            script: |
              helm uninstall gitea -n gitea
```
Deploy it
```shell
kubectl apply -f my-platform.yaml
```
Check the pods created and if gitea is running, access its url: `https://gitea.localtest.me:8443`

**Note**: To clean up the cluster, simply delete the manifest file installed !

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
