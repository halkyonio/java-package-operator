package io.halkyon.platform.qute;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.quarkus.qute.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class MultilinesScriptTest {

    @Inject
    @Location("helmscript")
    Template tmpl;

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
