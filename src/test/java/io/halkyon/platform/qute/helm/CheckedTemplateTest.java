package io.halkyon.platform.qute.helm;

import io.halkyon.platform.operator.model.Helm;
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

        Helm helm = new Helm();
        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setRepoName("nginx-ingress");
        helm.setChart(chart);

        assertEquals(expected,Templates.helm(helm).render());
    }

    @CheckedTemplate(basePath = "")
    public static class Templates {
        static native TemplateInstance helm(Helm helm);
    }

}
