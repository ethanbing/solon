package org.noear.solon.cloud.extend.water.integration.http;

import org.noear.solon.Utils;
import org.noear.solon.cloud.extend.water.service.CloudJobServiceWaterImp;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.water.WaterClient;

/**
 * @author noear
 * @since 1.4
 */
public class HandlerJob implements Handler {
    @Override
    public void handle(Context ctx) throws Throwable {
        String ip = ctx.realIp();

        if (WaterClient.Whitelist.existsOfClientAndServerIp(ip)) {
            handleDo(ctx, ctx.param("name"));
        } else {
            ctx.output((ip + ",not is whitelist!"));
        }
    }

    private void handleDo(Context ctx, String name) {
        Handler handler = CloudJobServiceWaterImp.instance.get(name);

        if (handler == null) {
            ctx.statusSet(400);
            ctx.output("CloudJob[" + name + "] no exists");
        } else {
            try {
                handler.handle(ctx);
                ctx.output("OK");
            } catch (Throwable ex) {
                EventBus.push(ex);
                ctx.output(Utils.throwableUnwrap(ex));
            }
        }
    }
}