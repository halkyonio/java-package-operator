package io.halkyon.platform.qute.waitfor;

import io.halkyon.platform.operator.Templates;
import io.halkyon.platform.operator.model.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class HttpCodeIssueTest {

    @Test
    public void testWaitForScript() {
        String expected = """
            {#let wait=step.waitCondition namespace=step.namespace}
            echo "Wait for {wait.endpoint.name} ready ..."
            
            TIMEOUT_SECONDS=180
            RETRY_INTERVAL=5
            URL="{wait.endpoint.protocol}://{wait.endpoint.name}.{namespace.name}:{wait.endpoint.port}/{wait.path}"
            
            while [ "$ELAPSED_TIME" -lt "$TIMEOUT_SECONDS" ]; do
              STATUS_CODE=$(curl -ks -o /dev/null -w "%{http_code}" --max-time ${RETRY_INTERVAL} ${URL})
            
              if [ "$STATUS_CODE" -eq 200 ]; then
                echo "Health check successful! Pod is ready. (Status: ${STATUS_CODE})"
                exit 0
              else
                echo "Attempt failed. Status: ${STATUS_CODE}. Retrying in ${RETRY_INTERVAL} seconds..."
                sleep ${RETRY_INTERVAL}
                ELAPSED_TIME=$((ELAPSED_TIME + RETRY_INTERVAL))
              fi
            done
            
            echo "Error: Health check failed after ${TIMEOUT_SECONDS} seconds. Pod did not return HTTP 200 OK."
            echo "Service checked: ${URL}"
            exit 1""";

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
