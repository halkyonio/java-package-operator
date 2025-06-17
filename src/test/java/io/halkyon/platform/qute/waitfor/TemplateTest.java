package io.halkyon.platform.qute.waitfor;

import io.halkyon.platform.operator.Templates;
import io.halkyon.platform.operator.model.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class TemplateTest {

    @Test
    public void testWaitForScript() {
        String expected = """
            echo "Wait for ingress-nginx-controller-admission ready ..."
            until curl -ks http://ingress-nginx-controller-admission.default:443/; do
              echo "Waiting for service to be ready..."
              sleep 5
            done""";

        Step step = new Step();
        Namespace namespace = new Namespace();
        WaitCondition waitCondition = new WaitCondition();
        waitCondition.setNamespace(namespace.getName());

        Endpoint endpoint = new Endpoint();
        endpoint.setName("ingress-nginx-controller-admission");
        endpoint.setPath("/healthz");
        endpoint.setPort(443);
        waitCondition.setEndpoint(endpoint);

        step.setWaitCondition(waitCondition);
        step.setNamespace(namespace);

        assertEquals(expected, Templates.waitscript(step).render());
    }

}
