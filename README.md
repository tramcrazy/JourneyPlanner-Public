# London Underground Journey Planner

This is an entirely custom-built webapp which lets you plan journeys on the London Underground network (note - this project is unofficial and not affiliated with Transport for London).

**You can try it out now at [journeyplanner.tramcrazy.com](https://journeyplanner.tramcrazy.com)!**

## Architecture of the system
The system is split into modules so that code can be reused for other projects in future.
### HTTP server
Part of the project is a custom-built HTTP server and templating engine called `tortoise`! (because I thought it would be slow - it turned out to be fast)

`tortoise` implements HTTP (currently just GET requests, POST might be added in future) by reading HTTP requests from users and responding with HTTP responses which it generates and sends over a TCP socket. There is no web framework in use; `tortoise` is the web framework! It reads HTML templates and renders webpages on the server-side by dynamically inserting data. The code is in [src/main/java/webServer](src/main/java/webServer).

There is also some client-side JavaScript code in [src/main/resources/static](src/main/resources/static) (which is also where you'll find the CSS and favicon images!). The system is predominantly server-side and renders HTML snippets server-side which are then inserted into pages by the JavaScript code when they need to be significantly updated.
The HTML templates for the project and for the tortoise subproject more generally are all in [src/main/resources/templates](src/main/resources/templates).
### Data storage
The project uses a SQLite database to store data about all the stations on the London Underground network. This database can be updated whenever any changes occur to the network and I do this on the server for the deployed version periodically.
Currently, I am working on imoproving the database interaction code so that the system works better with multiple clients simultaneously. I also might move to a different SQL database such as Postgres to improve database performance. All the code related to the database is in [src/main/java/dataStorage](src/main/java/dataStorage).

### API interaction
When data needs to be downloaded to create the database, or when live data about train arrivals needs to be fetched, the [TfL API](https://api-portal.tfl.gov.uk) is used to fetch the necessary data. Note that I am not using TfL's pathfinding algorithm as I have implemented my own!
Code for this functionality is in [src/main/java/apiInteraction](src/main/java/apiInteraction).

### Pathfinding
When the server starts, the whole London Underground network is loaded into memory as a graph with an adjacency list stored in a Java HashMap. This allows the A* pathfinding algorithm I have implemented to quickly traverse between stations without many database calls.
The A* pathfinding algorithm prioritises routes to explore based on a heuristic related to the distance between stations. The code for all of this is in [src/main/java/pathfinding](src/main/java/pathfinding).

## Building
You will need a recent version of the Java Development Kit (JDK) and [Apache Maven](https://maven.apache.org/install.html) in order to build the project.

Clone this GitHub repository:
```bash
git clone https://github.com/tramcrazy/JourneyPlanner-Public.git
```
Move into the repository:
```bash
cd JourneyPlanner-Public
```
Compile and package the project:
```bash
mvn package
```
## Running
You should now find two outputted `.jar` files in the `target` folder which Maven will have created in your working directory.
You probably want the one with dependencies bundled (`JourneyPlanner-Public-[version]-jar-with-dependencies.jar`).

First you need to configure the `.env` configuration file which stores the API details as follows:
```
TFL_API_KEY=your_api_key_goes_here
TFL_API_ROOT=https://api.tfl.gov.uk/
```

You'll then need to download data for the database:
```bash
cd target
java -jar JourneyPlanner-Public-1.0-SNAPSHOT-jar-with-dependencies.jar update
```
Then you can run the server:
```bash
java -jar JourneyPlanner-Public-1.0-SNAPSHOT-jar-with-dependencies.jar run
```
You should be able to access the system on port 80 on your local device!