package io.halkyon.platform;

import io.halkyon.platform.operator.Templates;
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

        Map<String, Map<?, ?>> data = new HashMap<>();

        Map<String, Object> values = new HashMap<>();
        values.put("name","hello");
        values.put("repoUrl", "https://kubernetes.github.io/ingress-nginx");
        values.put("namespace", "nginx-ingress");
        values.put("helmValues", helmValues);
        values.put("createNamespace", false);
        data.put("s", values);

        var tmpl = Templates.helmscript();
        String result = tmpl.data(data).render();
        //Assertions.assertSame(expected,result);
    }

}
