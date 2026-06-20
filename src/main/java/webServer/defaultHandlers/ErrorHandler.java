package webServer.defaultHandlers;

import webServer.pageHandlers.PageHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static webServer.HelperLibrary.*;

public class ErrorHandler extends PageHandler {
    private final int errorCode;
    private final String url;

    public ErrorHandler(int errorCode, String url) {
        super();
        this.errorCode = errorCode;
        this.url = url;
    }

    @Override
    public byte[] getPage() {
        String htmlData = generateHtml();
        byte[] htmlBytes = htmlData.getBytes(StandardCharsets.UTF_8);
        return generateHttpResponse(errorCode,
                htmlBytes,
                "text/html; charset=UTF-8",
                htmlBytes.length);
    }

    @Override
    public byte[] getPage(String[][] queryParams) {
        return getPage();
    }

    private String generateHtml() {
        String templateHtml;
        try {
            InputStream fileStream = getClass().getResourceAsStream("/templates/tortoise/error.html");
            assert fileStream != null;
            templateHtml = new String(fileStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to read the template HTML. Perhaps the file doesn't exist.");
        }
        return templateHtml.formatted(errorCode, errorCode, chooseMessage(errorCode), url);
    }
}
