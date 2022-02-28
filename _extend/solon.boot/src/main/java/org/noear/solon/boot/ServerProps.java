package org.noear.solon.boot;

import org.noear.solon.Solon;

public class ServerProps {
    public static final int request_maxRequestSize;
    public static final int request_maxHeaderSize;
    public static final int session_timeout;
    public static final String session_state_domain;
    public static final boolean output_meta;
    public static final String encoding_request;
    public static final String encoding_response;

    static {
        String tmp = Solon.cfg().get("server.request.maxRequestSize", "").trim().toLowerCase();//k数
        {
            if (tmp.endsWith("mb")) {
                int val = Integer.parseInt(tmp.substring(0, tmp.length() - 2));
                request_maxRequestSize = val * 1204 * 1204;
            } else if (tmp.endsWith("kb")) {
                int val = Integer.parseInt(tmp.substring(0, tmp.length() - 2));
                request_maxRequestSize = val * 1204;
            } else if (tmp.length() > 0) {
                request_maxRequestSize = Integer.parseInt(tmp); //支持-1
            } else {
                request_maxRequestSize = 0;//默认0，表示不设置
            }
        }

        tmp = Solon.cfg().get("server.request.maxHeaderSize", "").trim().toLowerCase();//k数
        {
            if (tmp.endsWith("mb")) {
                int val = Integer.parseInt(tmp.substring(0, tmp.length() - 2));
                request_maxHeaderSize = val * 1204 * 1204;
            } else if (tmp.endsWith("kb")) {
                int val = Integer.parseInt(tmp.substring(0, tmp.length() - 2));
                request_maxHeaderSize = val * 1204;
            } else if (tmp.length() > 0) {
                request_maxHeaderSize = Integer.parseInt(tmp); //支持-1
            } else {
                request_maxHeaderSize = 0;//默认0，表示不设置
            }
        }

        session_timeout = Solon.cfg().getInt("server.session.timeout", 0);
        session_state_domain = Solon.cfg().get("server.session.state.domain");
        output_meta = Solon.cfg().getInt("solon.output.meta", 0) > 0;

        encoding_request = Solon.cfg().get("solon.encoding.request", Solon.encoding());
        encoding_response = Solon.cfg().get("solon.encoding.response", Solon.encoding());
    }
}