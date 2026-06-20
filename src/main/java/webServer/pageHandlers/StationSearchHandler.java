package webServer.pageHandlers;

import dataStorage.DbManager;
import webServer.HelperLibrary;
import webServer.HttpRequestMethod;

import java.nio.charset.StandardCharsets;

public class StationSearchHandler extends PageHandler {
    private final DbManager dbManager;

    public StationSearchHandler(DbManager dbManager) {
        super();
        this.dbManager = dbManager;
        this.allowedMethods = new HttpRequestMethod[]{HttpRequestMethod.GET};
    }

    @Override
    public byte[] getPage() {
        return getPage(new String[0][0]);
    }

    @Override
    public byte[] getPage(String[][] queryParams) {
        String[][] stations;
        byte[] listBytes;
        if (queryParams.length != 0 && queryParams[0][0].equals("query") && queryParams[0][1] != null && !queryParams[0][1].isEmpty()) {
            stations = dbManager.searchStations(queryParams[0][1]);
            String listHtml = generateSearchResults(stations);
            listBytes = listHtml.getBytes(StandardCharsets.UTF_8);
        } else {
            listBytes = "".getBytes(StandardCharsets.UTF_8);
        }
        return HelperLibrary.generateHttpResponse(200,
                listBytes,
                "text/html; charset=UTF-8",
                listBytes.length);
    }

    private String generateSearchResults(String[][] searchResults) {
        if (searchResults.length == 0) {
            return "";
        }

        StringBuilder listBuilder = new StringBuilder("<div class=\"list-group mb-3\">\n");
        String[] currentStationLineIds;

        for (String[] searchResult : searchResults) {
            currentStationLineIds = dbManager.getLineIdsAtStation(searchResult[0]);
            listBuilder.append("    <button type=\"button\" class=\"list-group-item list-group-item-action button-search-result\" data-station-id=\"%s\" data-station-name=\"%s\">\n".formatted(searchResult[0], searchResult[1]));
            listBuilder.append("        ").append(searchResult[1]).append("\n");
            for (String lineId : currentStationLineIds) {
                listBuilder.append("        ").append("<span class=\"span-dot bg-colour-").append(lineId).append("\"></span>\n");
            }
            listBuilder.append("    </button>\n");
        }

        listBuilder.append("</div>");
        return listBuilder.toString();
    }
}
