package io.halkyon.platform;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.ValueResolver;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class QuteTemplateTest {

    @Inject
    @Location("helmscript")
    Template helmscript;

    /*
    private String bashScript = """
                cat << EOF > values.yml
                ingress:
                  enabled: true
                EOF
                helm repo add kubernetes.github.io/ingress-nginx
                helm repo update
                helm install nginx-ingress ingress-nginx/ingress-nginx \\\\
                   --version 4.12.2 \\\\
                   --namespace ingress-nginx \\\\
                   --create-namespace \\\\
                   -f values.yml
            """;
    */

    @Test
    public void testScript() {
        String helmValues = """
            controller:
              hostPort:
                enabled: true
              service:
                type: NodePort
            ingress:
              enabled: true""";

        var template = """
            cat << EOF > values.yml
            {s.helmValues}
            EOF
            
            helm repo add ingress {s.repoUrl}
            helm repo update
            
            helm install nginx-ingress ingress/ingress-nginx {#if s.version??}--version {s.version}{/}{#if s.namespace??}--namespace {s.namespace}{/}{#if s.createNamespace}}--create-namespace{/} -f values.yml
            """;

        Map<String, Map<?, ?>> data = new HashMap<>();

        Map values = new HashMap();
        values.put("repoUrl", "https://kubernetes.github.io/ingress-nginx");
        values.put("namespace", "nginx-ingress");
        values.put("helmValues", helmValues);
        // values.put("version", "1.0");
        values.put("createNamespace", false);
        data.put("s", values);

        /*
        final Engine engine = Engine.builder()
            .addDefaults()
            .build();
        Template parsedTemplate = engine.parse(template);
        var result = parsedTemplate.data(data).render();
        */

        var result = helmscript.data(data).render();
        assertTrue(result.contains("echo Hello"));
    }

    @Test
    void testMultilinesScript() {
        var simpleScript = """
            echo Hello
            echo world
            """;

        var template = """
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              name: package-operator-test
            spec:
              template:
                spec:
                  containers:
                    - name: {s.name}
                      image: {s.image}
                      command:
                      - /bin/sh
                      - -c
                      - |
                        {s.script.indent(12)}
            """;

        var commands = Arrays.asList("/bin/sh", "-c", simpleScript);

        Map<String, Map<?, ?>> data = new HashMap<>();

        Map values = new HashMap();
        values.put("name", "helm-install");
        values.put("image", "dtzar/helm-kubectl");
        values.put("script", simpleScript);
        data.put("s", values);

        ValueResolver indentResolver = ValueResolver.builder()
            .appliesTo(c -> c.getName().equals("indent")
                && (c.getBase() instanceof String || c.getBase() instanceof byte[]))
            .applyToBaseClass(String.class)
            .resolveSync(context -> {
                String indentation = context.getParams().get(0).getParts().get(0).getName();
                if (context.getBase() instanceof byte[] bytes) {
                    return new String(bytes).replace("\n", "\n" + " ".repeat(Integer.parseInt(indentation)));
                } else {
                    return context.getBase().toString().replace("\n", "\n" + " ".repeat(Integer.parseInt(indentation)));
                }
            }).build();

        final Engine engine = Engine.builder()
            .addDefaultValueResolvers()
            .addValueResolver(indentResolver).build();
        Template parsedTemplate = engine.parse(template);
        var result = parsedTemplate.data(data).render();
        // System.out.println(result);

        Deployment deploy = Serialization.unmarshal(result,Deployment.class);
        assertEquals(commands, deploy.getSpec().getTemplate().getSpec().getContainers().get(0).getCommand());
    }

}
