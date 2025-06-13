package io.halkyon.platform.operator;

import io.halkyon.platform.operator.model.Helm;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@CheckedTemplate(requireTypeSafeExpressions = false)
public class Templates {
    public static native TemplateInstance helmscript(Helm helm);
    public static native TemplateInstance newhelmscript(Helm helm);
}
