package io.halkyon.platform.operator.controller;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.halkyon.platform.operator.crd.Package;
import io.halkyon.platform.operator.crd.PackageSpec;
import io.halkyon.platform.operator.crd.Platform;
import io.halkyon.platform.operator.crd.PlatformStatus;
import io.halkyon.platform.operator.model.Condition;
import io.halkyon.platform.operator.model.PackageDefinition;
import io.javaoperatorsdk.operator.api.config.informer.InformerEventSourceConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.halkyon.platform.operator.PackageUtils.*;
import static io.halkyon.platform.operator.controller.PackageReconciler.createContainersFromPipeline;

/*
@Workflow(
    dependents = {
        @Dependent(type = PackageDR.class),
    })
*/
public class PlatformReconciler implements Reconciler<Platform>, Cleaner<Platform> {
    private final static Logger LOG = LoggerFactory.getLogger(PlatformReconciler.class);

    @Override
    public List<EventSource<?, Platform>> prepareEventSources(EventSourceContext<Platform> context) {
        var packageEventSource =
            new InformerEventSource<>(
                InformerEventSourceConfiguration.from(Package.class, Platform.class)
                    .withLabelSelector(PACKAGE_LABEL_SELECTOR)
                    .build(),
                context);
        return List.of(packageEventSource);
    }

    public UpdateControl<Platform> reconcile(Platform platform, Context<Platform> context) {

        var name = platform.getMetadata().getName();
        List<PackageDefinition> pkgs;
        PackageDefinition pkgDefinition = null;

        LOG.info("Reconciling platform {}", name);

        // Verify first if packages have been declared !
        if (!platform.getSpec().getPackages().isEmpty()) {
            pkgs = platform.getSpec().getPackages();
        } else {
            LOG.warn("No packages declared part of the Platform CR");
            return null;
        }

        var previousPackages = context.getSecondaryResources(Package.class);
        if (previousPackages == null) {
            // When the previous package is null, then we process the first package of the list
            pkgDefinition = pkgs.getFirst();
            pkgDefinition.setCounter(1);
            LOG.info("No previous package found. Let's pick up the first");
        } else {
            // As a Package has already been processed, we will check if status is "installation succeeded"
            // and remove it from the list to be processed to pick up the next one
            List<Package> succeededPkgList = previousPackages.stream()
                .filter(pkg ->
                    pkg.getStatus() != null &&
                    pkg.getStatus().getInstallationStatus() != null &&
                    INSTALLATION_SUCCEEDED.equalsIgnoreCase(pkg.getStatus().getInstallationStatus())).collect(Collectors.toList());

            // Count how many packages succeeded to increase the counter
            Integer counter = (int)succeededPkgList.size();

            // Create a set of the succeeded names
            Set<String> succeededPackages = succeededPkgList.stream()
                .map(pkg -> pkg.getMetadata().getName())
                .collect(Collectors.toSet());

            List<PackageDefinition> remaining = pkgs.stream()
                .filter(pkg -> !succeededPackages.contains(pkg.getName()))
                .toList();

            Optional<PackageDefinition> nextToProcess = remaining.stream().findFirst();
            if (nextToProcess.isPresent()) {
                LOG.info("Next package to create: " + nextToProcess.get().getName());
                pkgDefinition = nextToProcess.get();
                pkgDefinition.setCounter(counter + 1);
            }
        }
        if (pkgDefinition != null) {
            var pkg = createPackageCR(pkgDefinition, platform);
            context.getClient()
                .resources(io.halkyon.platform.operator.crd.Package.class)
                .resource(pkg)
                .serverSideApply();

            PlatformStatus pStatus = platform.getStatus();
            if (pStatus == null) {
                pStatus = new PlatformStatus();
            }
            Condition condition = new Condition();
            condition.setMessage(String.format("Deploying the package: %s", pkgDefinition.getName()));
            condition.setType("Deploying");
            pStatus.addCondition(condition);
            platform.setStatus(pStatus);
            LOG.info("Updating the status of the platform: {} for the package: {}", platform.getMetadata().getName(), pkg.getMetadata().getName());
            return UpdateControl.patchStatus(platform);
        } else {
            return UpdateControl.noUpdate();
        }

    }


    private Package createPackageCR(PackageDefinition pkgDefinition, Platform platform) {
        Package pkg = new Package();
        pkg.getMetadata().setName(pkgDefinition.getName());
        pkg.getMetadata().setNamespace(platform.getMetadata().getNamespace());
        pkg.getMetadata().setLabels(createPackageLabels(pkg, pkgDefinition.getCounter()));
        pkg.addOwnerReference(platform);

        PackageSpec pkgSpec = new PackageSpec();
        pkgSpec.setName(pkgDefinition.getName());
        pkgSpec.setDescription(pkgDefinition.getDescription());
        pkgSpec.setPipeline(pkgDefinition.getPipeline());
        pkg.setSpec(pkgSpec);
        return pkg;
    }


    @Override
    public DeleteControl cleanup(Platform platform, Context<Platform> context) throws Exception {
        LOG.info("Platform resource: {} deleted", platform.getMetadata().getName());
        Set<Package> pkgs = context.getSecondaryResources(Package.class);
        List<Package> sortedPkgs = sortPackagesByOrderLabel(pkgs);

        sortedPkgs.forEach(pkg -> {
            LOG.info("Creating a job to uninstall the package {}, having position: {}", pkg.getMetadata().getName(),pkg.getMetadata().getLabels().get(PACKAGE_ORDER_LABEL));
            var containers = createContainersFromPipeline(
                pkg,
                s -> s.getName() != null &&
                    s.getName().startsWith("install") &&
                    s.getHelm() != null || s.getManifest() != null,
                true);

            // Adding also the user's script defined part of the step: uninstall
            containers.addAll(createContainersFromPipeline(
                pkg,
                s -> s.getName() != null && s.getName().startsWith("uninstall"),
                true));

            if (!containers.isEmpty()) {
                Job job = new JobBuilder()
                //@formatter:off
                .withNewMetadata()
                  .withName("uninstall-"+pkg.getMetadata().getName())
                  .withNamespace(pkg.getMetadata().getNamespace())
                  .withLabels(createResourceLabels(pkg))
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
                    .withName("uninstall-" + pkg.getMetadata().getName())
                    .waitUntilCondition(c ->
                        c != null &&
                            c.getStatus() != null &&
                            c.getStatus().getSucceeded() != null &&
                            c.getStatus().getSucceeded().equals(1), 60, TimeUnit.SECONDS);
            }
            LOG.info("Job to uninstall the package {} succeeded.", pkg.getMetadata().getName());
        });
        return DeleteControl.defaultDelete();
    }
}