package dataStorage;

import apiInteraction.DataGrabber;
import apiInteraction.responseTypes.Line;
import apiInteraction.responseTypes.Status;
import apiInteraction.responseTypes.TimetableResponse;
import io.github.cdimascio.dotenv.Dotenv;
import pathfinding.Adjacency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class DbManager {

    private final RdbmsHandler rdbmsHandler;
    private final DataGrabber dataGrabber;

    public DbManager(String databasePath, Dotenv dotenv) {
        rdbmsHandler = new RdbmsHandler(databasePath);
        dataGrabber = new DataGrabber(dotenv.get("TFL_API_ROOT"), dotenv.get("TFL_API_KEY"));
    }

    public void fillLinesAndRouteSectionsTables() {

        // Check if we need to create the Line table
        if (rdbmsHandler.checkTableDoesNotExist("Line")) {
            String[] lineFields = new String[] {"LineID VARCHAR(20) NOT NULL", "LineName VARCHAR(25) NOT NULL"};
            rdbmsHandler.createTable("Line", lineFields, "LineID", new String[]{}, new String[]{}, new String[]{});
        }

        // Check if we need to create the RouteSection table
        if (rdbmsHandler.checkTableDoesNotExist("RouteSection")) {
            String[] routeSectionFields = new String[] {"LineID VARCHAR(20) NOT NULL", "Originator CHAR(10) NOT NULL", "Destination CHAR(10) NOT NULL"};
            rdbmsHandler.createTable("RouteSection", routeSectionFields, "", new String[]{"LineID"}, new String[]{"Line"}, new String[]{"LineID"});
        }

        // Delete all previous lines and route sections
        rdbmsHandler.deleteAllRows("Line");
        rdbmsHandler.deleteAllRows("RouteSection");

        // Download and format data
        Line[] lines = dataGrabber.getLines("tube");
        String[][] lineNameValues = new String[lines.length][2];

        for (int i = 0; i < lines.length; i++) {
            lineNameValues[i][0] = prepString(lines[i].getId());
            lineNameValues[i][1] = prepString(lines[i].getName());
        }

        // Fill the Line table
        rdbmsHandler.fillTable("Line", lineNameValues);

        // Loop through all RouteSections and fill the RouteSection table
        String[] routeSectionData = new String[3];
        for (Line line : lines) {
            for (Line.RouteSection routeSection : line.getRouteSections()) {
                routeSectionData[0] = prepString(line.getId());
                routeSectionData[1] = prepString(routeSection.getOriginator());
                routeSectionData[2] = prepString(routeSection.getDestination());
                rdbmsHandler.fillTable("RouteSection", new String[][]{routeSectionData});
            }
        }
    }

    public void updateDisruptions() {

        // Check if we need to create the Disruption table
        if (rdbmsHandler.checkTableDoesNotExist("Disruption")) {
            String[] disruptionFields = new String[] {"DisruptionID CHAR(36) NOT NULL", "ClosureText VARCHAR(20) NOT NULL", "Description VARCHAR(500) NOT NULL", "LineID VARCHAR(20) NOT NULL"};
            rdbmsHandler.createTable("Disruption", disruptionFields, "DisruptionID", new String[]{"LineID"}, new String[]{"Line"}, new String[]{"LineID"});
        }

        // Delete all previous disruptions
        rdbmsHandler.deleteAllRows("Disruption");

        // Get all Tube Line IDs
        String[] lineIds = rdbmsHandler.selectStringField("Line", "LineID");
        Status.LineStatus.Disruption[] currentLineDisruptions;
        String[][] currentDisruptionData;

        // Get disruptions by line and fill table with the data
        for (int i = 0; i < lineIds.length; i++) {

            currentLineDisruptions = dataGrabber.getDisruptions(lineIds[i]);

            for (Status.LineStatus.Disruption disruption : currentLineDisruptions) {
                if (disruption != null) {
                    currentDisruptionData = new String[][] {{prepString(UUID.randomUUID().toString()), prepString(disruption.getClosureText()), prepString(disruption.getDescription()), prepString(lineIds[i])}};
                    rdbmsHandler.fillTable("Disruption", currentDisruptionData);
                }
            }
        }
    }

    public String[] getValidLineDestinationStationIds(String originStationId, String destinationStationId, String lineId) {
        return rdbmsHandler.selectDistinctStringFieldWithJoin(
                "Interval",
                "Timetable",
                "Interval.TimetableID",
                "Timetable.TimetableID",
                new String[]{"Interval.OriginStationID", "Interval.DestinationStationID", "Timetable.LineID"},
                new String[]{prepString(originStationId), prepString(destinationStationId), prepString(lineId)},
                "Timetable.DestinationStationID");
    }

    public String[][] getDisruptionLineNamesAndIds() {
        String[] affectedIds = rdbmsHandler.selectStringField("Disruption", "LineID");

        HashSet<String> affectedIdsSet = new HashSet<>(Arrays.asList(affectedIds));
        String[] uniqueIds = affectedIdsSet.toArray(new String[0]);
        String[][] uniqueIdsWithNames = new String[uniqueIds.length][2];

        for (int i = 0; i < uniqueIds.length; i++) {
            uniqueIdsWithNames[i][0] = uniqueIds[i];
            // Only bothering to get one line name as there is theoretically a 1-1 relationship between LineName and LineID
            uniqueIdsWithNames[i][1] = rdbmsHandler.selectStringFieldWhere("Line", "LineID", prepString(uniqueIds[i]), "LineName")[0];
        }

        return uniqueIdsWithNames;
    }

    public String[][] searchStations(String searchTerm) {
        return rdbmsHandler.selectStringFieldsWhereLike("Station", "StationName", searchTerm, new String[]{"StationID", "StationName"});
    }

    public String[] getLineIdsAtStation(String stationId) {
        return rdbmsHandler.selectStringFieldWhere("LineAtStation", "StationID", prepString(stationId), "LineID");
    }

    public String[] getAllStationIds() {
        return rdbmsHandler.selectStringField("Station", "StationID");
    }

    public String[][] getStationCoords(String stationId) {
        return rdbmsHandler.selectStringFieldsWhere("Station", "StationID", prepString(stationId), new String[]{"StationLat", "StationLon"});
    }

    public String getStationName(String stationId) {
        return rdbmsHandler.selectStringFieldWhere("Station", "StationID", prepString(stationId), "StationName")[0];
    }

    public String getLineName(String lineId) {
        return rdbmsHandler.selectStringFieldWhere("Line", "LineID", prepString(lineId), "LineName")[0];
    }

    public void fillStationAndTimetableTables() {

        if (rdbmsHandler.checkTableDoesNotExist("Station")) {
            String[] stationFields = new String[] {"StationID CHAR(11) NOT NULL", "StationName VARCHAR(30) NOT NULL", "StationLat FLOAT NOT NULL", "StationLon FLOAT NOT NULL"};
            rdbmsHandler.createTable("Station", stationFields, "StationID", new String[]{}, new String[]{}, new String[]{});
        }

        if (rdbmsHandler.checkTableDoesNotExist("LineAtStation")) {
            String[] lineAtStationFields = new String[] {"StationID CHAR(11) NOT NULL", "LineID VARCHAR(20) NOT NULL"};
            rdbmsHandler.createTable("LineAtStation", lineAtStationFields, "", new String[]{"StationID", "LineID"}, new String[]{"Station", "Line"}, new String[]{"StationID", "LineID"});
        }

        if (rdbmsHandler.checkTableDoesNotExist("Timetable")) {
            String[] timetableFields = new String[]{"TimetableID CHAR(36) NOT NULL", "OriginStationID CHAR(11) NOT NULL", "DestinationStationID CHAR(11) NOT NULL", "LineID VARCHAR(20) NOT NULL"};
            rdbmsHandler.createTable("Timetable", timetableFields, "TimetableID", new String[]{"OriginStationID", "DestinationStationID", "LineID"}, new String[]{"Station", "Station", "Line"}, new String[]{"StationID", "StationID", "LineID"});
        }

        if (rdbmsHandler.checkTableDoesNotExist("Interval")) {
            String[] intervalFields = new String[]{"TimetableID CHAR(36) NOT NULL", "OriginStationID CHAR(11) NOT NULL", "DestinationStationID CHAR(11) NOT NULL", "Minutes TINYINT NOT NULL"};
            rdbmsHandler.createTable("Interval", intervalFields, "", new String[]{"TimetableID", "OriginStationID", "DestinationStationID"}, new String[]{"Timetable", "Station", "Station"}, new String[]{"TimetableID", "StationID", "StationID"});
        }

        // Delete all previous Stations, LineAtStations, Timetables and Intervals
        rdbmsHandler.deleteAllRows("Station");
        rdbmsHandler.deleteAllRows("LineAtStation");
        rdbmsHandler.deleteAllRows("Timetable");
        rdbmsHandler.deleteAllRows("Interval");

        String[][] routeSections = rdbmsHandler.selectStringFields("RouteSection", new String[]{"LineID", "Originator", "Destination"});

        TimetableResponse timetableResponse;
        String[] stopNameArray;
        String[] newStopNameArray;
        String newStopName;
        String[] stopData;
        String[] lasData;

        for (String[] routeSection : routeSections) {
            timetableResponse = dataGrabber.getTimetable(routeSection[0], routeSection[1], routeSection[2]);
            if (timetableResponse != null) {
                for (TimetableResponse.Stop stop : timetableResponse.getStops()) {
                    // Logic to remove unnecessary "Underground Station" at end of stop names
                    stopNameArray = stop.getName().split(" ");
                    newStopNameArray = new String[stopNameArray.length - 2];
                    System.arraycopy(stopNameArray, 0, newStopNameArray, 0, stopNameArray.length - 2);
                    newStopName = String.join(" ", newStopNameArray);

                    // Check if stop already exists in table, otherwise add it
                    if (rdbmsHandler.checkRowDoesNotExist("Station", new String[]{"StationID"}, new String[]{stop.getId()})) {
                        stopData = new String[]{prepString(stop.getId()), prepString(newStopName), String.valueOf(stop.getLat()), String.valueOf(stop.getLon())};
                        rdbmsHandler.fillTable("Station", new String[][]{stopData});
                    }

                    // Check if LaS already exists in table, otherwise add it
                    if (rdbmsHandler.checkRowDoesNotExist("LineAtStation", new String[]{"StationID", "LineID"}, new String[]{stop.getId(), routeSection[0]})) {
                        lasData = new String[]{prepString(stop.getId()), prepString(routeSection[0])};
                        rdbmsHandler.fillTable("LineAtStation", new String[][]{lasData});
                    }
                }
                saveTimetable(timetableResponse);
            } else {
                System.out.println("Non-critical error - null timetable response. Skipping...");
            }
        }

    }

    public String prepString(String string) {
        String[] stringArray = string.split("");

        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i].equals("'")) {
                stringArray[i] = "''";
            }
        }

        return "'%s'".formatted(String.join("", stringArray));
    }

    private void saveTimetable(TimetableResponse timetableResponse) {
        String originStationId = timetableResponse.getTimetable().getDepartureStopId();
        String[][] timetableFields;
        String[][] intervalFields;
        String previousStationId;
        int currentMinutes;
        int intervalMinutes;

        for (TimetableResponse.Timetable.Route route : timetableResponse.getTimetable().getRoutes()) {
            for (TimetableResponse.Timetable.Route.StationInterval stationInterval : route.getStationIntervals()) {
                String timetableId = UUID.randomUUID().toString();
                TimetableResponse.Timetable.Route.StationInterval.Interval[] intervals = stationInterval.getIntervals();
                String destinationStationId = intervals[intervals.length - 1].getStopId();
                String lineId = timetableResponse.getLineId();

                timetableFields = new String[][]{{prepString(timetableId), prepString(originStationId), prepString(destinationStationId), prepString(lineId)}};
                rdbmsHandler.fillTable("Timetable", timetableFields);

                previousStationId = originStationId;
                currentMinutes = 0;

                for (TimetableResponse.Timetable.Route.StationInterval.Interval interval : stationInterval.getIntervals()) {
                    intervalMinutes = (int) (interval.getTimeToArrival() - currentMinutes);
                    currentMinutes += intervalMinutes;
                    intervalFields = new String[][]{{prepString(timetableId), prepString(previousStationId), prepString(interval.getStopId()), String.valueOf(intervalMinutes)}};
                    rdbmsHandler.fillTable("Interval", intervalFields);
                    previousStationId = interval.getStopId();
                }
            }
        }
    }

    public Adjacency[] getAdjacencies(String stationId) {
        String[][] adjacencies = rdbmsHandler.selectStringFieldsWhere("Interval", "OriginStationID", prepString(stationId), new String[]{"TimetableID", "DestinationStationID", "Minutes"});
        ArrayList<Adjacency> adjacencyList = new ArrayList<>();
        String lineId;

        outerLoop:
        for (String[] adjacency : adjacencies) {
            lineId = rdbmsHandler.selectStringFieldWhere("Timetable", "TimetableID", prepString(adjacency[0]), "LineID")[0];

            for (Adjacency adjacencyObject : adjacencyList) {
                if (adjacencyObject.getLineId().equals(lineId) && adjacencyObject.getDestinationStationId().equals(adjacency[1])) {
                    adjacencyObject.addMinuteRecord(Integer.parseInt(adjacency[2]));
                    continue outerLoop;
                }
            }

            String[] destinationCoords = getStationCoords(adjacency[1])[0];
            Adjacency adjacencyObject = new Adjacency(lineId, stationId, adjacency[1], Float.parseFloat(destinationCoords[0]), Float.parseFloat(destinationCoords[1]));
            adjacencyObject.addMinuteRecord(Integer.parseInt(adjacency[2]));
            adjacencyList.add(adjacencyObject);
        }

        return adjacencyList.toArray(new Adjacency[0]);
    }

    public DataGrabber getDataGrabber() {
        return dataGrabber;
    }
}
