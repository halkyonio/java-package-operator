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
import java.util.function.Function;
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
            //pod.addFinalizer("packages.halkyon.io/finalizer");

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
        LOG.info("Creating a job to uninstall the package {}",pkg.getMetadata().getName());

        var containers = createContainersFromPipeline(
            pkg,
            s -> s.getName() != null && s.getName().startsWith("install") && s.getHelm() != null,
            s -> generatePodCommandFromTemplate(s, Mode.UNINSTALL)
        );
        if (!containers.isEmpty()) {
            Job job = new JobBuilder()
                //@formatter:off
                .withNewMetadata()
                  .withName("uninstall-"+pkg.getMetadata().getName())
                  .withNamespace(pkg.getMetadata().getNamespace())
                  .withLabels(createPackageLabels(pkg))
                .endMetadata()
                .withNewSpec()
                  .withNewTemplate()
                    .withNewSpec()
                      .withContainers(containers)
                      .withRestartPolicy("Never")
                    .endSpec()
                  .endTemplate()
                .endSpec()
                .build();
                //@formatter:on
            job.addOwnerReference(pkg);
            LOG.debug("job generated: {}", Serialization.asYaml(job));
            context.getClient().resources(Job.class).inNamespace(pkg.getMetadata().getNamespace()).resource(job).serverSideApply();

            // Include a wait for condition till the job succeeded
            context.getClient().resources(Job.class)
                .inNamespace(pkg.getMetadata().getNamespace())
                .withName("uninstall-"+pkg.getMetadata().getName())
                .waitUntilCondition(c ->
                    c != null &&
                        c.getStatus() != null &&
                        c.getStatus().getSucceeded() != null &&
                        c.getStatus().getSucceeded().equals(1), 60, TimeUnit.SECONDS);
        }
        LOG.info("Waiting till the job to uninstall succeeded !");
        return DeleteControl.defaultDelete();
    }

    public PackageStatus updatePackageStatus(String status, Package pkg) {
        PackageStatus packageStatus = new PackageStatus();
        packageStatus.setName(pkg.getMetadata().getName());
        packageStatus.setInstallationStatus(status);
        return packageStatus;
    }

    public List<Container> createInitOrContainersFromPipeline(Package pkg, String action) {
        return Optional.ofNullable(pkg)
            .map(Package::getSpec)
            .map(PackageSpec::getPipeline)
            .map(Pipeline::getSteps)
            .orElse(Collections.emptyList())
            .stream()
            .filter(s -> s.getName() != null && s.getName().startsWith(action))
            .map(s -> {
                // TODO: To be improved if a trick exists to generate the object even if not defined within the YAML
                if (s.getNamespace() == null) {
                    s.setNamespace(new Namespace());
                }
                ContainerBuilder builder = new ContainerBuilder()
                    .withName(s.getName())
                    .withImage(s.getImage());
                if (s.getScript() != null && !s.getScript().isEmpty()) {
                    builder.withCommand(generatePodCommandFromScript(s.getScript()));
                } else {
                    builder.withCommand(generatePodCommandFromTemplate(s,getMode(action)));
                }
                return builder.build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Creates a list of Kubernetes Container objects based on the provided package pipeline steps.
     *
     * @param pkg The Package object containing the pipeline steps.
     * @param stepFilter A {@link Predicate} to determine which steps should be processed into containers.
     * This allows for flexible filtering logic (e.g., by name prefix, by presence of Helm config).
     * @param commandGenerator A {@link Function} that takes a {@link Step} and returns the
     * appropriate {@link List<String>} command for the container.
     * This encapsulates the logic for `generatePodCommandFromScript` vs `generatePodCommandFromTemplate`.
     * @return A {@link List} of {@link Container} objects. Returns an empty list if no steps are found or no steps match the filter.
     */
    public List<Container> createContainersFromPipeline(
        Package pkg,
        Predicate<Step> stepFilter,
        Function<Step, List<String>> commandGenerator) {

        return Optional.ofNullable(pkg)
            .map(Package::getSpec)
            .map(PackageSpec::getPipeline)
            .map(Pipeline::getSteps)
            .orElse(Collections.emptyList()) // Provide an empty list if any part of the path is null
            .stream()
            .filter(stepFilter) // Apply the custom filtering logic
            .map(s -> {
                // Common logic for all containers, regardless of filter or command generation
                // This 'namespace' initialization was common to both original methods
                if (s.getNamespace() == null) {
                    s.setNamespace(new Namespace());
                }

                ContainerBuilder builder = new ContainerBuilder()
                    .withName(s.getName())
                    .withImage(s.getImage());

                // Apply the custom command generation logic using the provided function
                builder.withCommand(commandGenerator.apply(s));

                return builder.build(); // Build the Kubernetes Container object
            })
            .collect(Collectors.toList()); // Collect all built containers into a list
    }

}