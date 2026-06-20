package pathfinding;

public class Node {

    private final String stationId;
    private final String parentId;
    private float timeToStation;
    private float timeFromStation;
    private String toNodeLineId;

    public Node(String stationId, String parentId) {
        this.stationId = stationId;
        this.parentId = parentId;
    }

    public void setToNodeLineId(String toNodeLineId) {
        this.toNodeLineId = toNodeLineId;
    }

    public String getToNodeLineId() {
        return toNodeLineId;
    }

    public String getStationId() {
        return stationId;
    }

    public String getParentId() {
        return parentId;
    }

    public float getTotalTime() {
        return timeToStation + timeFromStation;
    }

    public float getTimeToStation() {
        return timeToStation;
    }

    public void setTimeToStation(float timeToStation) {
        this.timeToStation = timeToStation;
    }

    public void setTimeFromStation(float timeFromStation) {
        this.timeFromStation = timeFromStation;
    }
}
