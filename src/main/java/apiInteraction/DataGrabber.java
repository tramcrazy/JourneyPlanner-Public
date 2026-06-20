package apiInteraction;

import apiInteraction.responseTypes.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

public class DataGrabber {

    private final String apiRoot;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final Random random;

    public DataGrabber(String apiRoot, String apiKey) {
        this.apiRoot = apiRoot;
        this.apiKey = apiKey;
        httpClient = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
        random = new Random();
    }

    public Line[] getLines(String mode) {
        String uri = apiRoot + "Line/Mode/" + mode + "/Route?app_key=" + apiKey;

        String responseJson = makeHttpRequest(uri);
        Line[] lines = null;

        try {
            lines = mapper.readValue(responseJson, Line[].class);
        } catch (JsonProcessingException e) {
            System.out.println("Error while trying to process JSON for line names and ids - returning null.");
            e.printStackTrace();
        }

        return lines;
    }

    public JourneyResponse getJourneyResponse(String originator, String destination, String date, String time) {
        String uri = apiRoot + "Journey/JourneyResults/" + originator + "/to/" + destination + "?mode=tube&date=" + date + "&time=" + time + "&app_key=" + apiKey;

        String responseJson = makeHttpRequest(uri);
        JourneyResponse journeyResponse = null;

        try {
            journeyResponse = mapper.readValue(responseJson, JourneyResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return journeyResponse;
    }

    public TimetableResponse getTimetable(String lineId, String originator, String destination) {
        String uri = apiRoot + "Line/" + lineId + "/Timetable/" + originator + "/to/" + destination + "?app_key=" + apiKey;

        String responseJson = makeHttpRequest(uri);
        TimetableResponse timetableResponse = null;

        try {
            timetableResponse = mapper.readValue(responseJson, TimetableResponse.class);
        } catch (JsonProcessingException e) {
            System.out.println("Error while trying to process JSON for timetable data - returning null.");
        } catch (IllegalArgumentException f) {
            System.out.println("Error while trying to process JSON for timetable data - no JSON present, returning null.");
        }

        return timetableResponse;
    }

    public Status.LineStatus.Disruption[] getDisruptions(String lineId) {
        String uri = apiRoot + "Line/" + lineId + "/Status?app_key=" + apiKey;

        String responseJson = makeHttpRequest(uri);
        Status.LineStatus.Disruption[] disruptions = null;
        Status[] statuses;
        Status.LineStatus[] lineStatuses;

        try {
            statuses = mapper.readValue(responseJson, Status[].class);
            lineStatuses = statuses[0].getLineStatuses();
            disruptions = new Status.LineStatus.Disruption[lineStatuses.length];
            for (int i = 0; i < lineStatuses.length; i++) {
                disruptions[i] = lineStatuses[i].getDisruption();
            }
        } catch (JsonProcessingException e) {
            System.out.println("Error while trying to process JSON for disruption data - returning null.");
            e.printStackTrace();
        }

        return disruptions;
    }

    public Arrival[] getArrivals(String stationId) {
        String uri = apiRoot + "StopPoint/" + stationId + "/Arrivals?app_key=" + apiKey;

        String responseJson = makeHttpRequest(uri);
        Arrival[] arrivals = null;

        try {
            arrivals = mapper.readValue(responseJson, Arrival[].class);
        } catch (JsonProcessingException e) {
            System.out.println("Error while trying to process JSON for arrival data - returning null.");
        }

        return arrivals;
    }

    private String makeHttpRequest(String uri) {
        boolean serverError = true;
        HttpResponse<String> response = null;

        while (serverError) {
            System.out.println("Trying to send request to " + uri);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();

            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("An error occurred while attempting to make an HTTP request to " + uri);
            }

            if (response == null) {
                throw new RuntimeException("Error while trying to fetch data from TfL - got null response. The API may be offline.");
            } else if (response.statusCode() != 200) {
                if (response.statusCode() == 404) {
                    System.out.println("Non-critical error while trying to fetch TfL data - data unexpectedly unavailable at endpoint " + uri);
                    return null;
                }
                System.out.println("Error while trying to fetch data from TfL - got unexpected HTTP response code " + response.statusCode());
                try {
                    Thread.sleep(random.nextInt(200, 500));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                serverError = false;
            }
        }
        return response.body();
    }
}
