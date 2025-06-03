package io.halkyon.platform.operator.controller;

import io.halkyon.platform.operator.crd.Package;
import io.halkyon.platform.operator.crd.PackageSpec;
import io.halkyon.platform.operator.crd.Platform;
import io.halkyon.platform.operator.crd.PlatformStatus;
import io.halkyon.platform.operator.model.PackageDefinition;
import io.javaoperatorsdk.operator.api.config.informer.InformerEventSourceConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.halkyon.platform.operator.PackageUtils.PACKAGE_LABEL_SELECTOR;
import static io.halkyon.platform.operator.PackageUtils.createPackageLabels;

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
        //List<PackageDefinition> pkgs;
        Map<Integer, PackageDefinition> indexedPackageMap;
        PackageDefinition pkgDefinition;

        LOG.info("Reconciling platform {}", name);

        // Verify first if packages have been declared !
        if (!platform.getSpec().getPackages().isEmpty()) {
            var pkgs = platform.getSpec().getPackages();
            indexedPackageMap = IntStream.range(0, pkgs.size())
                .boxed()
                .collect(Collectors.toMap(
                    i -> i + 1,
                    pkgs::get
                ));
        } else {
            LOG.warn("No packages declared part of the Platform CR");
            return null;
        }

        var previousPackage = context.getSecondaryResource(Package.class).orElse(null);
        if (previousPackage == null) {
            // When the previous package is null, then we will process the first package of the list
            pkgDefinition = indexedPackageMap.get(1);
        } else {
            if (previousPackage.getStatus() != null && previousPackage.getStatus().getMessage().equals("installation succeeded")) {
                LOG.info("Package installation succeeded for {}. Processing now the next package ...",previousPackage.getMetadata().getName());

            } else {
                LOG.warn("Package status is null or different ... {}",previousPackage);
            }
            return UpdateControl.noUpdate();
        }

        var pkg = createPackageCR(pkgDefinition, platform);
        context.getClient()
            .resources(io.halkyon.platform.operator.crd.Package.class)
            .resource(pkg)
            .serverSideApply();

        PlatformStatus pStatus = new PlatformStatus();
        pStatus.setMessage(String.format("Processing the package: %s", pkgDefinition.getName()));
        pStatus.setPackages();
        platform.setStatus(pStatus);
        return UpdateControl.patchStatus(platform);
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