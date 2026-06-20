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

        if (args.length > 0) {
            if (args[0].equals("update")) {
                dbManager.fillLinesAndRouteSectionsTables();
                dbManager.updateDisruptions();
                dbManager.fillStationAndTimetableTables();
            } else if (args[0].equals("run")) {
                StationGraph stationGraph = new StationGraph(dbManager);
                stationGraph.fillMap();
                startServer(dbManager, stationGraph);
            } else {
                System.out.println("Unknown command. Valid commands are update (to update the database) or run (to start the server).");
            }
        } else {
            System.out.println("No command provided. Valid commands are update (to update the database) or run (to start the server).");
        }
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
