package io.halkyon.platform.operator.controller;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.halkyon.platform.operator.Mode;
import io.halkyon.platform.operator.crd.Package;
import io.halkyon.platform.operator.crd.PackageSpec;
import io.halkyon.platform.operator.crd.PackageStatus;
import io.halkyon.platform.operator.model.Namespace;
import io.halkyon.platform.operator.model.Pipeline;
import io.halkyon.platform.operator.model.Step;
import io.javaoperatorsdk.operator.api.config.informer.InformerEventSourceConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
            LOG.info("Creating the pod for the package {}, having as position: {}",pkg.getMetadata().getName(),pkg.getMetadata().getLabels().get(PACKAGE_ORDER_LABEL));
            Pod pod = new PodBuilder()
                //@formatter:off
                .withNewMetadata()
                  .withName(pkg.getMetadata().getName())
                  .withNamespace(pkg.getMetadata().getNamespace())
                  .withLabels(createResourceLabels(pkg))
                .endMetadata()
                .withNewSpec()
                  .withInitContainers(createContainersFromPipeline(
                      pkg,
                      s -> s.getName() != null && s.getName().startsWith("init"),
                      false)
                  )
                  .withContainers(createContainersFromPipeline(
                      pkg,
                      s -> s.getName() != null && s.getName().startsWith("install"),
                      false)
                  )
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
         return DeleteControl.defaultDelete();
    }

    public PackageStatus updatePackageStatus(String status, Package pkg) {
        PackageStatus packageStatus = new PackageStatus();
        packageStatus.setName(pkg.getMetadata().getName());
        packageStatus.setInstallationStatus(status);
        return packageStatus;
    }

    /**
     * Creates a list of Kubernetes Container objects based on the provided package pipeline steps.
     *
     * @param pkg The Package object containing the pipeline steps.
     * @param stepFilter A {@link Predicate} to determine which steps should be processed into containers.
     * This allows for flexible filtering logic (e.g., by name prefix, by presence of Helm config).
     * @param cleanUpFlag A {@link boolean} indicating if we will delete the resources of a package
     * @return A {@link List} of {@link Container} objects. Returns an empty list if no steps are found or no steps match the filter.
     */
    public static List<Container> createContainersFromPipeline(
        Package pkg,
        Predicate<Step> stepFilter,
        boolean cleanUpFlag) {

        List<Step> stepsList = Optional.ofNullable(pkg)
            .map(Package::getSpec)
            .map(PackageSpec::getPipeline)
            .map(Pipeline::getSteps)
            .orElse(Collections.emptyList());

        return stepsList.stream()
            .filter(stepFilter)
            .map(s -> {
                // Create a namespace object to get the default values
                if (s.getNamespace() == null) {
                    s.setNamespace(new Namespace());
                }

                Optional<Mode> actionMode = determineAction(s, cleanUpFlag);
                List<String> command;
                if (actionMode.isPresent()) {
                    Mode mode = actionMode.get();
                    command = generatePodCommand(s, mode);
                } else {
                    throw new RuntimeException("No specific action mode determined for step: " + s.getName() + ". Operation aborted.");
                }

                ContainerBuilder builder = new ContainerBuilder()
                    .withName(s.getName())
                    .withImage(s.getImage())
                    .withCommand(command);

                return builder.build(); // Build the Kubernetes Container object
            })
            .collect(Collectors.toList()); // Collect all built containers into a list
    }

}