import dataStorage.DbManager;
import io.github.cdimascio.dotenv.Dotenv;
import pathfinding.StationGraph;
import webServer.Server;
import webServer.pageHandlers.DisruptionsHandler;
import webServer.pageHandlers.HomeHandler;
import webServer.pageHandlers.ResultHandler;
import webServer.pageHandlers.StationSearchHandler;

public class Main {
    public static void main(String[] args) {

        // Initialise environment variables and database
        Dotenv dotenv = Dotenv.load();
        DbManager dbManager = new DbManager("journeyPlannerDatabase.db", dotenv);

        // Download and save data for lines, disruptions, stations, timetables - not always necessary but should really be run at least daily.
//        dbManager.fillLinesAndRouteSectionsTables();
//        dbManager.updateDisruptions();
//        dbManager.fillStationAndTimetableTables();

        StationGraph stationGraph = new StationGraph(dbManager);
        stationGraph.fillMap();
        startServer(dbManager, stationGraph);

    }

    public static void startServer(DbManager dbManager, StationGraph stationGraph) {
        // Create server
        System.out.println("Starting server...");
        Server server = new Server(80);

        // Initialize page handlers
        HomeHandler homeHandler = new HomeHandler(dbManager);
        ResultHandler resultHandler = new ResultHandler(dbManager, stationGraph);
        DisruptionsHandler disruptionsHandler = new DisruptionsHandler(dbManager);
        StationSearchHandler searchHandler = new StationSearchHandler(dbManager);

        // Register pages with server
        server.registerPage("/", homeHandler);
        server.registerPage("/result", resultHandler);
        server.registerPage("/api/disruptions", disruptionsHandler);
        server.registerPage("/api/search", searchHandler);

        // Start server
        server.start();
    }
}
