package pathfinding;

import dataStorage.DbManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class StationGraph {

    private final HashMap<String, StationData> stationMap;
    private final DbManager dbManager;
    private final float tubeAverageSpeedKmPerMin = 0.55F;
    private final float earthRadiusKm = 6371F;
    private final float tubeJourneyVsStraightLineDistanceMultiplier = 1.45F;

    public StationGraph(DbManager dbManager) {
        this.dbManager = dbManager;
        stationMap = new HashMap<>();
    }

    public void fillMap() {
        String[] stationIds = dbManager.getAllStationIds();
        Adjacency[] stationAdjacencies;
        float[] stationFloatCoords;
        String[][] stationCoords;
        StationData stationData;
        for (String stationId : stationIds) {
            stationAdjacencies = dbManager.getAdjacencies(stationId);
            stationCoords = dbManager.getStationCoords(stationId);
            stationFloatCoords = new float[]{Float.parseFloat(stationCoords[0][0]), Float.parseFloat(stationCoords[0][1])};
            stationData = new StationData(stationAdjacencies, stationFloatCoords);
            stationMap.put(stationId, stationData);
        }
    }

    private float haversineDistanceKm(float lat1, float lon1, float lat2, float lon2) {
        float latDifference = (float) (Math.toRadians(lat1) - Math.toRadians(lat2));
        float lonDifference = (float) (Math.toRadians(lon1) - Math.toRadians(lon2));
        return (float) (2 * earthRadiusKm * Math.asin(Math.sqrt(Math.sin(latDifference / 2) * Math.sin(latDifference / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDifference / 2) * Math.sin(lonDifference / 2))));
    }

    public float estimateTime(String station1Id, String station2Id) {
        float[] station1Coords = stationMap.get(station1Id).stationCoords();
        float[] station2Coords = stationMap.get(station2Id).stationCoords();
        float distance = tubeJourneyVsStraightLineDistanceMultiplier * haversineDistanceKm(station1Coords[0], station1Coords[1], station2Coords[0], station2Coords[1]);
        return distance / tubeAverageSpeedKmPerMin;
    }

    public Path findPath(String origin, String destination) {
        ArrayList<Node> openList = new ArrayList<>();
        ArrayList<Node> closedList = new ArrayList<>();
        float shortestTime;
        int shortestTimeIndex;
        float currentStationTime;
        Node currentNode = null;
        Node currentSuccessorNode;
        Adjacency[] currentNodeAdjacencies;
        String finalLineId = "";
        boolean foundPath = false;
        ArrayList<String[]> edgeList = new ArrayList<>();
        float finalTime = 0;
        float[] finalTimesBetween = new float[0];

        Node startNode = new Node(origin, "");
        openList.add(startNode);

        outerLoop:
        while (!openList.isEmpty()) {
            shortestTime = Float.MAX_VALUE;
            shortestTimeIndex = 0;

            for (int i = 0; i < openList.size(); i++) {
//                currentStationTime = openList.get(i).getTimeToStation(); // previous logic - didn't realise this was semi-Dijkstra's and not A* lol
                currentStationTime = openList.get(i).getTotalTime(); // testing proper A*
                if (currentStationTime < shortestTime) {
                    shortestTime = currentStationTime;
                    shortestTimeIndex = i;
                }
            }

            currentNode = openList.remove(shortestTimeIndex);
            currentNodeAdjacencies = stationMap.get(currentNode.getStationId()).adjacencies();

            successorLoop:
            for (Adjacency adjacency : currentNodeAdjacencies) {
                if (adjacency.getDestinationStationId().equals(destination)) {
                    finalLineId = adjacency.getLineId();
                    finalTime = adjacency.getAvgMinutes();
                    foundPath = true;
                    break outerLoop;
                }

                currentSuccessorNode = new Node(adjacency.getDestinationStationId(), currentNode.getStationId());
                currentSuccessorNode.setToNodeLineId(adjacency.getLineId());
                currentSuccessorNode.setTimeToStation(currentNode.getTimeToStation() + adjacency.getAvgMinutes());
                currentSuccessorNode.setTimeFromStation(estimateTime(adjacency.getDestinationStationId(), destination));

                for (Node openListNode : openList) {
                    if (openListNode.getStationId().equals(currentSuccessorNode.getStationId()) && openListNode.getTotalTime() < currentSuccessorNode.getTotalTime()) {
                        continue successorLoop;
                    }
                }

                for (Node closedListNode : closedList) {
                    if (closedListNode.getStationId().equals(currentSuccessorNode.getStationId()) && closedListNode.getTotalTime() < currentSuccessorNode.getTotalTime()) {
                        continue successorLoop;
                    }
                }

                openList.add(currentSuccessorNode);
            }

            closedList.add(currentNode);
        }

        if (foundPath) {
            edgeList.add(new String[]{finalLineId, destination});
            float shortestTimeToParent;
            int shortestPathToParentIndex;
            ArrayList<Float> totalTimes = new ArrayList<>();

            while (!currentNode.getParentId().isEmpty()) {
                totalTimes.add(currentNode.getTimeToStation());

                edgeList.add(new String[]{currentNode.getToNodeLineId(), currentNode.getStationId()});
                shortestTimeToParent = Float.MAX_VALUE;
                shortestPathToParentIndex = 0;

                for (int i = 0; i < closedList.size(); i++) {
                    if (closedList.get(i).getStationId().equals(currentNode.getParentId()) && closedList.get(i).getTimeToStation() < shortestTimeToParent) {
                        shortestTimeToParent = closedList.get(i).getTimeToStation();
                        shortestPathToParentIndex = i;
                    }
                }

                currentNode = closedList.get(shortestPathToParentIndex);
            }

            float[] timesBetween = getTimesBetween(convertFloatList(totalTimes));
            float[] finalTimeBetween = new float[]{finalTime};

            finalTimesBetween = new float[timesBetween.length + 1];
            System.arraycopy(timesBetween, 0, finalTimesBetween, 0, timesBetween.length);
            System.arraycopy(finalTimeBetween, 0, finalTimesBetween, timesBetween.length, 1);
        }

        Collections.reverse(edgeList);
        return new Path(edgeList.toArray(new String[0][0]), finalTimesBetween);
    }

    private float[] getTimesBetween(float[] cumulativeTimes) {
        float[] timesBetween = new float[cumulativeTimes.length];

        if (cumulativeTimes.length != 0) {
            timesBetween[0] = cumulativeTimes[cumulativeTimes.length - 1];
            int j = 1;

            for (int i = cumulativeTimes.length - 2; i > -1; i--) {
                timesBetween[j] = cumulativeTimes[i] - cumulativeTimes[i + 1];
                j++;
            }
        }

        return timesBetween;
    }

    private float[] convertFloatList(ArrayList<Float> list) {
        float[] array = new float[list.size()];

        for (int i = 0; i < array.length; i++) {
            array[i] = (list.get(i) != null ? list.get(i) : Float.NaN);
        }

        return array;
    }
}
