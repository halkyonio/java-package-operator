package io.halkyon.platform.qute.helm;

import io.halkyon.platform.operator.Templates;
import io.halkyon.platform.operator.model.Helm;
import io.halkyon.platform.operator.model.Namespace;
import io.halkyon.platform.operator.model.Step;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class CheckedTemplateTest {

    @Test
    public void testHelmRepoAdd() {
        String expected = "helm repo add nginx-ingress https://kubernetes.github.io/ingress-nginx";

        Step step = new Step();
        Helm helm = new Helm();
        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setName("nginx-ingress");

        helm.setChart(chart);
        step.setHelm(helm);
        assertEquals(expected,TestTemplates.helmrepo(step).render());
    }

    @Test
    public void testHelmChartName() {
        String expected = """
            cat << 'EOF' > values.yml
            EOF
            
            helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
            helm repo update
            
            helm install my-ingress-nginx ingress-nginx/ingress-nginx --namespace default --create-namespace -f values.yml
            """;

        Step step = new Step();
        Namespace namespace = new Namespace();

        Helm helm = new Helm();

        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setName("ingress-nginx");

        Helm.Release release = new Helm.Release();
        release.setName("my-ingress-nginx");

        helm.setChart(chart);
        helm.setRelease(release);

        step.setHelm(helm);
        step.setNamespace(namespace);

        assertEquals(expected, Templates.helmscript(step).render());
    }

    @Test
    public void testHelmInstall() {
        String helmValues = """
            controller:
              hostPort:
                enabled: true
              service:
                type: NodePort
            ingress:
              enabled: true
            """;
        String expected = """
            cat << 'EOF' > values.yml
            controller:
              hostPort:
                enabled: true
              service:
                type: NodePort
            ingress:
              enabled: true
            EOF
            
            helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
            helm repo update
            
            helm install ingress-nginx ingress-nginx/ingress-nginx --namespace default --create-namespace -f values.yml
            """;

        Step step = new Step();
        Namespace namespace = new Namespace();

        Helm helm = new Helm();

        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setName("ingress-nginx");
        helm.setChart(chart);
        helm.setValues(helmValues);

        step.setHelm(helm);
        step.setNamespace(namespace);

        assertEquals(expected, Templates.helmscript(step).render());
    }

    // TODO: To be reviewed as don't work ...
    @Disabled
    @Test
    public void testHelmInstallAndMultiLines() {
        String helmValues = """
            controller:
              hostPort:
                enabled: true
              service:
                type: NodePort
            ingress:
              enabled: true
            """;
        String expected = """
            cat << 'EOF' > values.yml
            controller:
              hostPort:
                enabled: true
              service:
                type: NodePort
            ingress:
              enabled: true
            EOF
            
            helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
            helm repo update
            
            helm install ingress-nginx ingress-nginx/ingress-nginx \\
            
              --namespace default \\
              --create-namespace \\
              -f values.yml""";

        Step step = new Step();
        Namespace namespace = new Namespace();

        Helm helm = new Helm();

        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setName("ingress-nginx");
        helm.setChart(chart);
        helm.setValues(helmValues);

        step.setHelm(helm);
        step.setNamespace(namespace);

        assertEquals(expected,TestTemplates.helminstallwithcr(step).render());
    }

    @Test
    public void testHelmUninstall() {
        String expected = "helm uninstall nginx-ingress --namespace default";

        Step step = new Step();
        Namespace namespace = new Namespace(); // create an instance to set the default values

        Helm helm = new Helm();
        Helm.Chart chart = new Helm.Chart();
        chart.setName("nginx-ingress");

        helm.setChart(chart);
        step.setHelm(helm);
        step.setNamespace(namespace);

        assertEquals(expected,TestTemplates.helmuninstall(step).render());
    }

    @CheckedTemplate(basePath = "")
    public static class TestTemplates {
        static native TemplateInstance helmrepo(Step step);
        static native TemplateInstance helminstallwithcr(Step step);
        static native TemplateInstance helmuninstall(Step step);
    }

}
