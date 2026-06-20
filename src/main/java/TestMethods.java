import apiInteraction.DataGrabber;
import apiInteraction.responseTypes.JourneyResponse;
import apiInteraction.responseTypes.Line;
import apiInteraction.responseTypes.Status;
import apiInteraction.responseTypes.TimetableResponse;
import dataStorage.DbManager;
import dataStorage.RdbmsHandler;
import io.github.cdimascio.dotenv.Dotenv;
import pathfinding.Adjacency;
import pathfinding.StationGraph;

public class TestMethods {
    public void testDatabase() {
        Dotenv dotenv = Dotenv.load();
        String apiRoot = "https://api.tfl.gov.uk/";
        String apiKey = dotenv.get("TFL_API_KEY");
        DataGrabber dataGrabber = new DataGrabber(apiRoot, apiKey);
        RdbmsHandler rdbmsHandler = new RdbmsHandler("journeyPlannerDatabase.db");
        // One-time table creation code
//        String[] lineFields = new String[] {"LineID VARCHAR(20) NOT NULL", "LineName VARCHAR(25) NOT NULL"};
//        rdbmsHandler.createTable("Line", lineFields, "LineID", "", "", "");
        Line[] lines = dataGrabber.getLines("tube");
        for (Line line : lines) {
            System.out.println(line.getId());

        }
        String[][] lineNameValues = new String[lines.length][2];
        for (int i = 0; i < lines.length; i++) {
            lineNameValues[i][0] = "'" + lines[i].getId() + "'";
            lineNameValues[i][1] = "'" + lines[i].getName() + "'";
        }
        rdbmsHandler.fillTable("Line", lineNameValues);

        rdbmsHandler.closeDbConnection();
    }

    public void initialiseDatabase() {
        RdbmsHandler rdbmsHandler = new RdbmsHandler("journeyPlannerDatabase.db");
        if (rdbmsHandler.checkTableDoesNotExist("Line")) {
            String[] lineFields = new String[] {"LineID VARCHAR(20) NOT NULL", "LineName VARCHAR(25) NOT NULL"};
            rdbmsHandler.createTable("Line", lineFields, "LineID", new String[]{}, new String[]{}, new String[]{});
        }
        if (rdbmsHandler.checkTableDoesNotExist("Disruption")) {
            String[] disruptionFields = new String[] {"DisruptionID CHAR(5) NOT NULL", "ClosureText VARCHAR(20) NOT NULL", "Description VARCHAR(500) NOT NULL", "LineID VARCHAR(20) NOT NULL"};
            rdbmsHandler.createTable("Disruption", disruptionFields, "DisruptionID", new String[]{"LineID"}, new String[]{"Line"}, new String[]{"LineID"});
        }
    }


    public void testDisruptions() {
        Dotenv dotenv = Dotenv.load();
        String apiRoot = dotenv.get("TFL_API_ROOT");
        String apiKey = dotenv.get("TFL_API_KEY");
        DataGrabber dataGrabber = new DataGrabber(apiRoot, apiKey);
        Line[] lines = dataGrabber.getLines("tube");
        String currentLineId;
        Status.LineStatus.Disruption[] currentLineDisruptions;
        for (Line line : lines) {
            currentLineId = line.getId();
            System.out.println(line.getName());
            currentLineDisruptions = dataGrabber.getDisruptions(currentLineId);
            for (Status.LineStatus.Disruption disruption : currentLineDisruptions) {
                if (disruption != null) {
                    System.out.println(disruption.getClosureText());
                    System.out.println(disruption.getDescription());
                }
            }
        }
    }

    public void testTimetableResponse() {
        Dotenv dotenv = Dotenv.load();
        String apiRoot = "https://api.tfl.gov.uk/";
        String apiKey = dotenv.get("TFL_API_KEY");
        DataGrabber dataGrabber = new DataGrabber(apiRoot, apiKey);
        Line[] lines = dataGrabber.getLines("tube");
        String lineName = lines[0].getName();
        System.out.println("LINE NAME: " + lineName);
        for (Line.RouteSection routeSection : lines[0].getRouteSections()) {
            System.out.println("ROUTE SECTION: " + routeSection.getName());
            String originator = routeSection.getOriginator();
            String destination = routeSection.getDestination();
            TimetableResponse timetableResponse = dataGrabber.getTimetable(lineName, originator, destination);
            for (TimetableResponse.Timetable.Route route :  timetableResponse.getTimetable().getRoutes()) {
                for (TimetableResponse.Timetable.Route.StationInterval stationInterval : route.getStationIntervals()) {
                    System.out.println(stationInterval.getId());
                    for (TimetableResponse.Timetable.Route.StationInterval.Interval interval : stationInterval.getIntervals()) {
                        System.out.println(interval.getStopId() + ": " + interval.getTimeToArrival());
                    }
                }
            }
        }
    }

    public static void testTtrStations() {
        Dotenv dotenv = Dotenv.load();
        String apiRoot = "https://api.tfl.gov.uk/";
        String apiKey = dotenv.get("TFL_API_KEY");
        DataGrabber dataGrabber = new DataGrabber(apiRoot, apiKey);
        Line[] lines = dataGrabber.getLines("tube");
        for (Line line : lines) {
            System.out.println("LINE NAME: " + line.getName());
            for (Line.RouteSection routeSection : line.getRouteSections()) {
                System.out.println("ROUTE SECTION: " + routeSection.getName());
                String originator = routeSection.getOriginator();
                String destination = routeSection.getDestination();
                TimetableResponse timetableResponse = dataGrabber.getTimetable(line.getId(), originator, destination);
                TimetableResponse.Station[] ttrStations = timetableResponse.getStations();
                for (TimetableResponse.Station station : ttrStations) {
                    System.out.println(station.getName());
                }
            }
        }
    }

    public static void testTtrStops() {
        Dotenv dotenv = Dotenv.load();
        String apiRoot = "https://api.tfl.gov.uk/";
        String apiKey = dotenv.get("TFL_API_KEY");
        DataGrabber dataGrabber = new DataGrabber(apiRoot, apiKey);
        Line[] lines = dataGrabber.getLines("tube");
        for (Line line : lines) {
            System.out.println("LINE NAME: " + line.getName());
            for (Line.RouteSection routeSection : line.getRouteSections()) {
                System.out.println("ROUTE SECTION: " + routeSection.getName());
                String originator = routeSection.getOriginator();
                String destination = routeSection.getDestination();
                TimetableResponse timetableResponse = dataGrabber.getTimetable(line.getId(), originator, destination);
                TimetableResponse.Stop[] ttrStops = timetableResponse.getStops();
                for (TimetableResponse.Stop stop : ttrStops) {
                    System.out.println(stop.getName() + " " + stop.getId());
                }
            }
        }
    }

    public static void testAdjacencies(DbManager dbManager, String stationId) {
        Adjacency[] adjacencies = dbManager.getAdjacencies(stationId);
        for (Adjacency adjacency : adjacencies) {
            System.out.println("--------------------------------");
            System.out.println("------------ADJACENCY-----------");
            System.out.println("Destination ID: " + adjacency.getDestinationStationId());
            System.out.println("Destination lat/lon: " + adjacency.getDestinationLat() + ", " + adjacency.getDestinationLon());
            System.out.println("Line ID: " + adjacency.getLineId());
            System.out.println("Minutes: " + adjacency.getAvgMinutes());
            System.out.println("--------------------------------");
        }
    }

    public static void testEstimates(StationGraph stationGraph, DbManager dbManager) {
        DataGrabber dataGrabber = dbManager.getDataGrabber();
        String[] stationIds = dbManager.getAllStationIds();
        int totalTime;
        for (int i = 0; i < 50; i++) {
            String randomStationId1 = stationIds[(int) (Math.random() * 272)];
            String randomStationId2 = stationIds[(int) (Math.random() * 272)];
            System.out.println("----------------------------");
            System.out.println("-----------JOURNEY----------");
            System.out.println("Origin: " + randomStationId1);
            System.out.println("Destination: " + randomStationId2);
            System.out.println("Estimate: " + stationGraph.estimateTime(randomStationId1, randomStationId2));
            totalTime = 0;
            JourneyResponse journeyResponse = dataGrabber.getJourneyResponse(randomStationId1, randomStationId2, "20260128", "1000");
            for (JourneyResponse.Journey.Leg leg : journeyResponse.getJourneys()[0].getLegs()) {
                if (leg.getMode().getId().equals("tube")) {
                    totalTime += leg.getDuration();
                }
            }
            System.out.println("Real time: " + totalTime);
        }

    }
}
