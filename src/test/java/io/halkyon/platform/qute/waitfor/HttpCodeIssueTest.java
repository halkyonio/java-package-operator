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
            
            TIMEOUT_SECONDS=180
            RETRY_INTERVAL=5
            ELAPSED_TIME=0
            
            URL="http://ingress-nginx-controller-admission.default:443//healthz"
            CMD="curl -i -o - -ks -X GET $URL -H \\"Content-Type: application/json\\""
            
            echo "Wait for $URL endpoint to be ready ..."
            
            while [ "$ELAPSED_TIME" -lt "$TIMEOUT_SECONDS" ]; do
              STATUS=$($CMD | grep HTTP | awk '{print $2}')
              if [ "$STATUS" != "200" ]; then
                echo "Attempt failed. Status: $STATUS. Retrying in $RETRY_INTERVAL seconds..."
                sleep $RETRY_INTERVAL
                ELAPSED_TIME=$((ELAPSED_TIME + RETRY_INTERVAL))
              else
                echo "Endpoint replied successfully. (Status: $STATUS)"
                exit 0
              fi
            done
            
            echo "Error: endpoint failed after $TIMEOUT_SECONDS seconds. Service did not return HTTP 200 OK."
            echo "Service checked: $URL"
            exit 1
            """;

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
