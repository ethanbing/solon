package org.noear.solon.view.freemarker.tags;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.extend.auth.AuthUtil;
import org.noear.solon.extend.auth.annotation.Logical;
import org.noear.solon.extend.auth.tags.TagAttrs;

import java.io.IOException;
import java.util.Map;

/**
 * @author noear
 * @since 1.4
 */
@Component("view:hasPermission")
public class HasPermissionTag implements TemplateDirectiveModel {
    @Override
    public void execute(Environment env, Map map, TemplateModel[] templateModels, TemplateDirectiveBody body) throws TemplateException, IOException {
        String nameStr = (String) map.get(TagAttrs.ATTR_name);
        String logicalStr = (String) map.get(TagAttrs.ATTR_logical);

        if (Utils.isEmpty(nameStr)) {
            return;
        }

        String[] names = nameStr.split(",");

        if (names.length == 0) {
            return;
        }

        if (AuthUtil.verifyPermissions(names, Logical.of(logicalStr))) {
            body.render(env.getOut());
        }
    }
}