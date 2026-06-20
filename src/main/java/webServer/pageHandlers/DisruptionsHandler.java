package webServer.pageHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dataStorage.DbManager;
import webServer.DisruptionContainer;
import webServer.HelperLibrary;
import webServer.HttpRequestMethod;

import java.nio.charset.StandardCharsets;

public class DisruptionsHandler extends PageHandler {
    private final DbManager dbManager;
    private final ObjectMapper mapper;

    public DisruptionsHandler(DbManager dbManager) {
        super();
        this.dbManager = dbManager;
        this.mapper = new ObjectMapper();
        this.allowedMethods = new HttpRequestMethod[]{HttpRequestMethod.GET};
    }

    @Override
    public byte[] getPage() {
        dbManager.updateDisruptions();
        String jsonData = generateJson();
        byte[] jsonBytes = jsonData.getBytes(StandardCharsets.UTF_8);
        return HelperLibrary.generateHttpResponse(200,
                jsonBytes,
                "application/json; charset=UTF-8",
                jsonBytes.length);
    }

    @Override
    public byte[] getPage(String[][] queryParams) {
        return getPage();
    }

    private String generateJson() {
        String[] disruptions = HelperLibrary.generateDisruptions(dbManager.getDisruptionLineNamesAndIds());
        DisruptionContainer container = new DisruptionContainer(disruptions);
        String jsonData;
        try {
            jsonData = mapper.writeValueAsString(container);
        } catch (JsonProcessingException e) {
            System.out.println("An error occurred when trying to convert disruption HTML to JSON.");
            throw new RuntimeException(e);
        }
        return jsonData;
    }
}
