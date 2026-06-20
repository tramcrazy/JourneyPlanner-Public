package webServer.pageHandlers;

import apiInteraction.DataGrabber;
import apiInteraction.responseTypes.Arrival;
import dataStorage.DbManager;
import pathfinding.Path;
import pathfinding.StationGraph;
import webServer.Stage;
import webServer.HelperLibrary;
import webServer.HttpRequestMethod;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class ResultHandler extends PageHandler {
    private final DbManager dbManager;
    private String origin = "";
    private String destination = "";
    private final StationGraph stationGraph;

    public ResultHandler(DbManager dbManager, StationGraph stationGraph) {
        super();
        this.dbManager = dbManager;
        this.stationGraph = stationGraph;
        this.allowedMethods = new HttpRequestMethod[]{HttpRequestMethod.GET};
    }

    @Override
    public byte[] getPage() {
        return getPage(new String[0][0]);
    }

    @Override
    public byte[] getPage(String[][] queryParams) {
        for (String[] queryParam : queryParams) {
            if (queryParam[0].equals("origin")) {
                origin = queryParam[1];
            } else if (queryParam[0].equals("destination")) {
                destination = queryParam[1];
            }
        }

        String[] originLines = dbManager.getLineIdsAtStation(origin);
        String[] destinationLines = dbManager.getLineIdsAtStation(destination);

        if (originLines.length == 0 || destinationLines.length == 0) {
            return getResultError("Invalid Station ID", "This might have been caused by a database issue or your browser. Try again later!");
        }

        Path foundPath = stationGraph.findPath(origin, destination);
        // System.out.println(Arrays.deepToString(foundPath.segments()));

        if (foundPath.segments().length == 0) {
            return getResultError("No Route Found", "This was probably caused by an issue on the server side. Try again later!");
        }

        Stage[] pathStages = getPathStages(foundPath);
        ArrayList<String> cardHtmlFragments = new ArrayList<>();

        for (int i = 0; i < pathStages.length; i++) {
//            System.out.println(pathStages[i].lineId());
//            System.out.println(pathStages[i].firstStationOnLine() + " to " + pathStages[i].finalStationOnLine());
//            System.out.println("second station: " + pathStages[i].secondStationOnLine());
//            System.out.println(pathStages[i].numStationsOnLine() + " stops");
//            System.out.println(pathStages[i].stageTime() + " minutes");
//            System.out.println();
            cardHtmlFragments.add(generateStandardCardHtml(pathStages[i]));

            if (i != pathStages.length - 1) {
                cardHtmlFragments.add(generateChangeCardHtml(pathStages[i], pathStages[i + 1]));
            }
        }

        String originName = dbManager.getStationName(pathStages[0].firstStationOnLine());
        String destinationName = dbManager.getStationName(pathStages[pathStages.length - 1].finalStationOnLine());

        String[] disruptionHtml = HelperLibrary.generateDisruptions(dbManager.getDisruptionLineNamesAndIds());
        String originHtml = generateStationHtml(originName, originLines);
        String destinationHtml = generateStationHtml(destinationName, destinationLines);
        String fragmentString = String.join(System.lineSeparator(), cardHtmlFragments);

        String pageHtml;
        try {
            URL pageURL = getClass().getClassLoader().getResource("templates/result.html");
            assert pageURL != null;
            pageHtml = new String(Files.readAllBytes(Paths.get(pageURL.toURI())));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to read the template HTML. Perhaps the file doesn't exist.");
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred - URI syntax issue.");
        }

        pageHtml = pageHtml.formatted(disruptionHtml[0],
                disruptionHtml[1],
                originHtml,
                destinationHtml,
                fragmentString);

        byte[] pageBytes = pageHtml.getBytes(StandardCharsets.UTF_8);
        return HelperLibrary.generateHttpResponse(200,
                pageBytes,
                "text/html; charset=UTF-8",
                pageBytes.length);
    }

    private String generateStationHtml(String stationName, String[] stationLineIds) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<h1>").append(stationName).append("</h1>");

        for (int i = 0; i < stationLineIds.length; i++) {
            htmlBuilder.append("<span class=\"span-dot bg-colour-").append(stationLineIds[i]).append("\"></span>").append(System.lineSeparator());

            if (i != stationLineIds.length - 1) {
                htmlBuilder.append(System.lineSeparator());
            }
        }

        return htmlBuilder.toString();
    }

    private byte[] getResultError(String header, String explanation) {
        String pageHtml;

        try {
            URL pageURL = getClass().getClassLoader().getResource("templates/resultError.html");
            assert pageURL != null;
            pageHtml = new String(Files.readAllBytes(Paths.get(pageURL.toURI())));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to read the template HTML. Perhaps the file doesn't exist.");
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred - URI syntax issue.");
        }

        pageHtml = pageHtml.formatted(header, explanation);

        byte[] pageBytes = pageHtml.getBytes(StandardCharsets.UTF_8);
        return HelperLibrary.generateHttpResponse(400,
                pageBytes,
                "text/html; charset=UTF-8",
                pageBytes.length);
    }

    private Stage[] getPathStages(Path foundPath) {
        String lastLineId = "";
        String firstStationOnLine = origin;
        String secondStationOnLine = "";
        String currentStation = "";
        int numStationsOnLine = 0;
        ArrayList<Stage> stageArrayList = new ArrayList<>();
        Stage workingStage;
        int workingIndex = 0;
        int numAdded;
        float currentTotalTime;

        for (int i = 0; i < foundPath.segments().length; i++) {
            currentStation = foundPath.segments()[i][1];

            if (i == 1) {
                if (foundPath.segments()[1][0].equals(foundPath.segments()[0][0])) {
                    secondStationOnLine = foundPath.segments()[1][1];
                } else {
                    secondStationOnLine = foundPath.segments()[0][1];
                }
            }

            if (foundPath.segments()[i][0].equals(lastLineId)) {
                numStationsOnLine++;
            } else {
                if (!lastLineId.isEmpty()) {
                    numStationsOnLine++;

                    numAdded = 0;
                    currentTotalTime = 0;

                    while (numAdded < numStationsOnLine - 1) {
                        currentTotalTime += foundPath.timesBetween()[workingIndex];
                        workingIndex++;
                        numAdded++;
                    }

                    workingStage = new Stage(lastLineId, firstStationOnLine, secondStationOnLine, foundPath.segments()[i - 1][1], numStationsOnLine - 1, currentTotalTime);
                    stageArrayList.add(workingStage);
                    firstStationOnLine = foundPath.segments()[i - 1][1];
                    secondStationOnLine = foundPath.segments()[i][1];
                }
                numStationsOnLine = 1;
                lastLineId = foundPath.segments()[i][0];
            }

            if (i == foundPath.segments().length - 1) {
                numAdded = 0;
                currentTotalTime = 0;

                while (numAdded < numStationsOnLine) {
                    currentTotalTime += foundPath.timesBetween()[workingIndex];
                    workingIndex++;
                    numAdded++;
                }

                workingStage = new Stage(lastLineId, firstStationOnLine, secondStationOnLine, currentStation, numStationsOnLine, currentTotalTime);
                stageArrayList.add(workingStage);
            }
        }

        return stageArrayList.toArray(new Stage[0]);
    }

    private String generateStandardCardHtml(Stage stage) {
        String templateHtml;

        try {
            URL templateURL = getClass().getClassLoader().getResource("templates/standardCard.html");
            assert templateURL != null;
            templateHtml = new String(Files.readAllBytes(Paths.get(templateURL.toURI())));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to read the template HTML. Perhaps the file doesn't exist.");
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred - URI syntax issue.");
        }

        String lineName = dbManager.getLineName(stage.lineId());
        String[] lineDestinationIds = dbManager.getValidLineDestinationStationIds(stage.firstStationOnLine(), stage.secondStationOnLine(), stage.lineId());

        String[] lineDestinationNames = new String[lineDestinationIds.length];
        String stationName;

        for (int i = 0; i < lineDestinationIds.length; i++) {
            stationName = dbManager.getStationName(lineDestinationIds[i]);
            lineDestinationNames[i] = stationName;
        }

        String destinationsJoined;
        if (lineDestinationNames.length == 0) {
            destinationsJoined = "<span class=\"fst-italic\">(No live data available)</span>";
        } else {
            destinationsJoined = "<span class=\"fw-bold\">" + String.join("</span>/<span class=\"fw-bold\">", lineDestinationNames) + "</span>";
        }

        DataGrabber dataGrabber = dbManager.getDataGrabber();
        Arrival[] arrivals = dataGrabber.getArrivals(stage.firstStationOnLine());
        ArrayList<Integer> timeSecondsToTrains = new ArrayList<>();

        for (Arrival arrival : arrivals) {
//            System.out.println(arrival.getLineId());
            for (String destinationId : lineDestinationIds) {
                if (arrival.getDestinationNaptanId() != null && arrival.getDestinationNaptanId().equals(destinationId) && arrival.getLineId().equals(stage.lineId())) {
                    timeSecondsToTrains.add(arrival.getTimeToStation());
                }
            }
        }

        Collections.sort(timeSecondsToTrains);

        String finalStationName = dbManager.getStationName(stage.finalStationOnLine());

        String timesHtml = generateTimesHtml(timeSecondsToTrains);
        return templateHtml.formatted(stage.lineId(), stage.lineId(), lineName, finalStationName, stage.numStationsOnLine(), (int) stage.stageTime(), destinationsJoined, timesHtml);
    }

    private String generateTimesHtml(ArrayList<Integer> times) {
        StringBuilder timesBuilder = new StringBuilder();
        if (times.isEmpty()) {
            return "<span class=\"fst-italic\">(No live data available)</span>";
        } else {
            for (int i = 0; i < times.size(); i++) {
                timesBuilder.append("<span class=\"fw-bold\">").append(times.get(i) / 60).append("</span>");
                if (i != times.size() - 1) {
                    timesBuilder.append(", ");
                }
            }
        }

        return timesBuilder.toString();
    }

    private String generateChangeCardHtml(Stage firstStage, Stage secondStage) {
        String templateHtml;

        try {
            URL templateURL = getClass().getClassLoader().getResource("templates/changeCard.html");
            assert templateURL != null;
            templateHtml = new String(Files.readAllBytes(Paths.get(templateURL.toURI())));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to read the template HTML. Perhaps the file doesn't exist.");
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occurred - URI syntax issue.");
        }

        String firstLineName = dbManager.getLineName(firstStage.lineId());
        String secondLineName = dbManager.getLineName(secondStage.lineId());

        String finalStationName = dbManager.getStationName(firstStage.finalStationOnLine());
        return templateHtml.formatted(finalStationName, firstStage.lineId(), firstLineName, secondStage.lineId(), secondLineName);
    }
}
