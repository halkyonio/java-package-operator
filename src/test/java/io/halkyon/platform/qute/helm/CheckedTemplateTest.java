package io.halkyon.platform.qute.helm;

import io.halkyon.platform.operator.model.Helm;
import io.halkyon.platform.operator.model.Namespace;
import io.halkyon.platform.operator.model.Step;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.test.junit.QuarkusTest;
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
        chart.setRepoName("nginx-ingress");

        helm.setChart(chart);
        step.setHelm(helm);
        assertEquals(expected,Templates.helmrepo(step).render());
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
            cat << EOF > values.yml
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
            
            helm install ingress-nginx ingress-nginx/ingress-nginx --namespace default --create-namespace -f values.yml""";

        Step step = new Step();
        Namespace namespace = new Namespace();

        Helm helm = new Helm();

        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setRepoName("ingress-nginx");
        helm.setChart(chart);
        helm.setValues(helmValues);

        step.setHelm(helm);
        step.setNamespace(namespace);

        assertEquals(expected,Templates.helminstall(step).render());
    }

    @CheckedTemplate(basePath = "")
    public static class Templates {
        static native TemplateInstance helmrepo(Step step);
        static native TemplateInstance helminstall(Step step);
    }

}
