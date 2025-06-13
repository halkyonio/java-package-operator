package io.halkyon.platform.qute.helm;

import io.halkyon.platform.operator.model.Helm;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.test.QuarkusUnitTest;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuteHelmTemplateTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
        .withApplicationRoot(root -> root
            .addClass(Templates.class)
            .addAsResource(new StringAsset("helm repo add {chart.repoName} {chart.repoUrl}"),"templates/helm.txt")
        );

    @Test
    public void testHelmRepoAdd() {
        String expected = "helm repo add ingress https://kubernetes.github.io/ingress-nginx";

        Helm helm = new Helm();
        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setRepoName("nginx-ingress");
        helm.setChart(chart);

        assertEquals(expected,Templates.helm(helm).render());
    }

    @CheckedTemplate(basePath = "", requireTypeSafeExpressions = false)
    public static class Templates {
        static native TemplateInstance helm(Helm helm);
    }

}
