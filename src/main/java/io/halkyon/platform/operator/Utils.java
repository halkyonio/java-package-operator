package io.halkyon.platform.operator;

import io.halkyon.platform.operator.crd.Platform;

public class Utils {
    public static String configMapName(Platform platform) {
        return platform.getMetadata().getName() + "-001";
    }
}
