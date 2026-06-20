package webServer.pageHandlers;

import webServer.HttpRequestMethod;

public abstract class PageHandler {
    protected HttpRequestMethod[] allowedMethods;

    public PageHandler() {
        this.allowedMethods = new HttpRequestMethod[]{};
    }

    public HttpRequestMethod[] getAllowedMethods() {
        return allowedMethods;
    }

    public abstract byte[] getPage();

    public abstract byte[] getPage(String[][] queryParams);
}
