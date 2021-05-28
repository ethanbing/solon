package org.noear.solon.extend.auth;

import java.util.function.Predicate;

/**
 * 认证适配器（需要用户对接）
 *
 * @author noear
 * @since 1.4
 */
public class AuthAdapter {
    private String loginUrl;
    private String loginProcessingUrl;
    private String usernameParam;
    private String passwordParam;
    private String logoutUrl;
    private Predicate<String> authUrlMatchers = (url) -> true;
    private AuthProcessor authProcessor;

    public String loginUrl() {
        return loginUrl;
    }

    /**
     * 登录Url
     */
    public AuthAdapter loginUrl(String url) {
        loginUrl = url;
        return this;
    }


    public String loginProcessingUrl() {
        return loginProcessingUrl;
    }

    /**
     * 登录处理Url
     */
    public AuthAdapter loginProcessingUrl(String url) {
        loginProcessingUrl = url;
        return this;
    }

    public String usernameParam() {
        return usernameParam;
    }

    /**
     * 用户名参数名
     */
    public AuthAdapter usernameParam(String name) {
        usernameParam = name;
        return this;
    }

    public String passwordParam() {
        return passwordParam;
    }

    /**
     * 密码参数名
     */
    public AuthAdapter passwordParam(String name) {
        passwordParam = name;
        return this;
    }

    public String logoutUrl() {
        return logoutUrl;
    }

    /**
     * 退出Url
     */
    public AuthAdapter logoutUrl(String url) {
        logoutUrl = url;
        return this;
    }

    public Predicate<String> authUrlMatchers(){
        return authUrlMatchers;
    }

    /**
     * 认证Url匹配
     * */
    public AuthAdapter  authUrlMatchers(Predicate<String> tester){
        authUrlMatchers = tester;
        return this;
    }


    public AuthProcessor authProcessor(){
        return authProcessor;
    }


    /**
     * 认证处理器匹配
     */
    public AuthAdapter authProcessor(AuthProcessor processor) {
        authProcessor = processor;
        return this;
    }


}