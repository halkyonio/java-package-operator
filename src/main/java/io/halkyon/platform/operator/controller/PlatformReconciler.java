package io.halkyon.platform.operator.controller;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.halkyon.platform.operator.PackageUtils.*;

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
        } else {
            // As a Package has already been processed, we will check its status is "installation succeeded"
            // and remove it from the list of the to be processed to pick up the next one
            Set<String> succeededNames = previousPackages.stream()
                .filter(pkg -> pkg.getStatus() != null && pkg.getStatus().getInstallationStatus() != null)
                .filter(pkg -> INSTALLATION_SUCCEEDED.equalsIgnoreCase(pkg.getStatus().getInstallationStatus()))
                .map(pkg -> pkg.getMetadata().getName())
                .collect(Collectors.toSet());

            List<PackageDefinition> remaining = pkgs.stream()
                .filter(pkg -> !succeededNames.contains(pkg.getName()))
                .toList();

            Optional<PackageDefinition> nextToProcess = remaining.stream().findFirst();
            if (nextToProcess.isPresent()) {
                LOG.info("Next package to create: " + nextToProcess.get().getName());
                pkgDefinition = nextToProcess.get();
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
            return UpdateControl.patchStatus(platform);
        } else {
            return UpdateControl.noUpdate();
        }

    }

    /**
     * Creates a new List<PackageDefinition> by combining an existing list
     * and a new PackageDefinition object.
     * The original list is not modified.
     *
     * @param originalList The list to which you want to conceptually "append".
     * @param newPackage The new object to append.
     * @return A new List containing all elements from the original list plus the newPackage.
     */
    public static List<PackageDefinition> appendPackage(
        List<PackageDefinition> originalList, PackageDefinition newPackage) {

        // If the original list is null, treat it as an empty list
        // and just create a new list containing only the new package.
        if (originalList == null) {
            return Collections.singletonList(newPackage);
        }

        return Stream.concat(
                originalList.stream(),
                Stream.of(newPackage)
            )
            .collect(Collectors.toList());
    }


    private Package createPackageCR(PackageDefinition pkgDefinition, Platform platform) {
        Package pkg = new Package();
        pkg.getMetadata().setName(pkgDefinition.getName());
        pkg.getMetadata().setNamespace(platform.getMetadata().getNamespace());
        pkg.getMetadata().setLabels(createPackageLabels(pkg));
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
        return DeleteControl.defaultDelete();
    }
}