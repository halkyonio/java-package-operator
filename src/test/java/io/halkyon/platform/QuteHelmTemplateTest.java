package io.halkyon.platform;

import io.halkyon.platform.operator.Templates;
import io.halkyon.platform.operator.model.Helm;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class QuteHelmTemplateTest {

    @Test
    public void testHelmScriptWithValues() {
        String helmValues = """
            controller:
              hostPort:
                enabled: true
              service:
                type: NodePort
            ingress:
              enabled: true""";

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
            
            helm repo add ingress https://kubernetes.github.io/ingress-nginx
            helm repo update
            
            helm install nginx-ingress ingress/ingress-nginx \
              --namespace nginx-ingress \
              -f values.yml
            """;

/*        Map<String, Map<?, ?>> data = new HashMap<>();

        Map<String, Object> values = new HashMap<>();
        values.put("name","hello");
        values.put("repoUrl", "https://kubernetes.github.io/ingress-nginx");
        values.put("namespace", "nginx-ingress");
        values.put("helmValues", helmValues);
        values.put("createNamespace", false);
        data.put("s", values);*/

        Helm helm = new Helm();
        Helm.Chart chart = new Helm.Chart();
        chart.setRepoUrl("https://kubernetes.github.io/ingress-nginx");
        chart.setRepoName("nginx-ingress");

        var tmpl = Templates.newhelmscript(helm);
        String result = tmpl.data(helm).render();
        //Assertions.assertSame(expected,result);
    }

}
