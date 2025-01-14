package demo;

import org.noear.solon.cloud.utils.http.HttpUtils;

/**
 * @author noear 2021/10/13 created
 */
public class DemoTest {
    public void demo1() throws Exception {
        //基于具体地址去调用
        //
        HttpUtils.http("http://x.x.x/demo").get();

        HttpUtils.http("http://x.x.x/demo").data("name", "noear").post();
    }

    public void demo2() throws Exception {
        //基于负载均衡去调用
        //
        HttpUtils.http("userapi", "/api/demo").get();

        HttpUtils.http("userapi", "/api/demo").data("name", "noear").post();
    }
}
