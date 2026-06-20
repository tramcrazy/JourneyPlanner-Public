package webServer.pageHandlers;

import dataStorage.DbManager;
import webServer.HelperLibrary;
import webServer.HttpRequestMethod;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HomeHandler extends PageHandler {
    private final DbManager dbManager;

    public HomeHandler(DbManager dbManager) {
        super();
        this.dbManager = dbManager;
        this.allowedMethods = new HttpRequestMethod[]{HttpRequestMethod.GET};
    }

    @Override
    public byte[] getPage() {
        String htmlData = generateHtml();
        byte[] htmlBytes = htmlData.getBytes(StandardCharsets.UTF_8);
        return HelperLibrary.generateHttpResponse(200,
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
            URL templateURL = getClass().getClassLoader().getResource("templates/home.html");
            assert templateURL != null;
            templateHtml = new String(Files.readAllBytes(Paths.get(templateURL.toURI())));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to read the template HTML. Perhaps the file doesn't exist.");
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred - URI syntax issue.");
        }
        String[] disruptionHtml = HelperLibrary.generateDisruptions(dbManager.getDisruptionLineNamesAndIds());
        return templateHtml.formatted(disruptionHtml[0], disruptionHtml[1]);
    }
}
