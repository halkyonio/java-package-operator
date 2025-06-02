package io.halkyon.platform.operator;

import io.halkyon.platform.operator.crd.platform.PlatformCR;

public class Utils {
    public static String configMapName(PlatformCR platformCR) {
        return platformCR.getMetadata().getName() + "-001";
    }
}
