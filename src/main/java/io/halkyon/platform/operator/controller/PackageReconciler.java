package io.halkyon.platform.operator.controller;

import io.fabric8.kubernetes.api.model.*;
import io.halkyon.platform.operator.crd.Package;
import io.halkyon.platform.operator.crd.PackageSpec;
import io.halkyon.platform.operator.crd.PackageStatus;
import io.halkyon.platform.operator.model.Pipeline;
import io.javaoperatorsdk.operator.api.config.informer.InformerEventSourceConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.halkyon.platform.operator.PackageUtils.*;

/*
@Workflow(
    dependents = {
        @Dependent(type = PackageDR.class),
    })
*/
public class PackageReconciler implements Reconciler<Package>, Cleaner<Package> {
    private final static Logger LOG = LoggerFactory.getLogger(PackageReconciler.class);

    @Override
    public List<EventSource<?, Package>> prepareEventSources(EventSourceContext<Package> context) {
        var podEventSource =
            new InformerEventSource<>(
                InformerEventSourceConfiguration.from(Pod.class, Package.class)
                    .withLabelSelector(PACKAGE_LABEL_SELECTOR)
                    .build(),
                context);
        return List.of(podEventSource);
    }

    public UpdateControl<Package> reconcile(Package pkg, Context<Package> context) {
        LOG.info("Processing the package {}", pkg.getMetadata().getName());

        var previousPod = context.getSecondaryResource(Pod.class).orElse(null);
        if (previousPod == null) {
            LOG.info("Creating the pod for the package {}",pkg.getMetadata().getName());
            Pod pod = new PodBuilder()
                //@formatter:off
                .withNewMetadata()
                  .withName(pkg.getMetadata().getName())
                  .withNamespace(pkg.getMetadata().getNamespace())
                  .withLabels(createPackageLabels(pkg))
                .endMetadata()
                .withNewSpec()
                  .withInitContainers(createInitOrContainersFromPipeline(pkg, "init"))
                  .withContainers(createInitOrContainersFromPipeline(pkg, "install"))
                  .withRestartPolicy("Never") // To avoid CrashLoopbackOff as pod is restarting
                .endSpec()
                .build();
                //@formatter:on
            pod.addOwnerReference(pkg);

            context.getClient().pods().inNamespace(pkg.getMetadata().getNamespace()).resource(pod).serverSideApply();
        } else {
            LOG.info("Status of the pod changed: {}",previousPod.getStatus().getPhase());
            if (previousPod.getStatus().getPhase().equals("Succeeded")) {
                pkg.setStatus(updatePackageStatus(INSTALLATION_SUCCEEDED,pkg));
                return UpdateControl.patchStatus(pkg);
            }
        }
        return UpdateControl.noUpdate();
    }

    @Override
    public DeleteControl cleanup(Package pkg, Context<Package> context) throws Exception {
        LOG.info("Creating a pod to uninstall the package {}",pkg.getMetadata().getName());

        // TODO: Investigate the best approach to run the uninstall pod before the package is deleted
        var containers = createInitOrContainersFromPipeline(pkg, "uninstall");
        if (!containers.isEmpty()) {
            Pod pod = new PodBuilder()
                //@formatter:off
                .withNewMetadata()
                  .withName("uninstall-"+pkg.getMetadata().getName())
                  .withNamespace(pkg.getMetadata().getNamespace())
                  .withLabels(createPackageLabels(pkg))
                  //.withFinalizers("packages.halkyon.io/finalizer")
                .endMetadata()
                .withNewSpec()
                  .withContainers(containers)
                  .withRestartPolicy("Never") // To avoid CrashLoopbackOff as pod is restarting
                .endSpec()
                .build();
                //@formatter:on
            //pod.addOwnerReference(pkg);
            context.getClient().pods().inNamespace(pkg.getMetadata().getNamespace()).resource(pod).serverSideApply();
        }
        LOG.info("Package resource deleted");
        return DeleteControl.defaultDelete();
    }

    public PackageStatus updatePackageStatus(String status, Package pkg) {
        PackageStatus packageStatus = new PackageStatus();
        packageStatus.setName(pkg.getMetadata().getName());
        packageStatus.setInstallationStatus(status);
        return packageStatus;
    }

    public List<Container> createInitOrContainersFromPipeline(Package pkg, String WordToSearch) {
        return Optional.ofNullable(pkg)
            .map(Package::getSpec)
            .map(PackageSpec::getPipeline)
            .map(Pipeline::getSteps)
            .orElse(Collections.emptyList())
            .stream()
            .filter(s -> s.getName() != null && s.getName().startsWith(WordToSearch))
            .map(s -> {
                ContainerBuilder builder = new ContainerBuilder()
                    .withName(s.getName())
                    .withImage(s.getImage());

                if (s.getScript() != null && !s.getScript().isEmpty()) {
                    List<String> prefixedCommands = Stream.concat(
                            Stream.of("/bin/bash", "-exc"),
                            Stream.of(s.getScript()))
                        .collect(Collectors.toList());
                    builder.withCommand(prefixedCommands);
                }

                return builder.build();
            })
            .collect(Collectors.toList());
    }

}