package pathfinding;

public class Adjacency {
    private String lineId;
    private String originStationId;
    private String destinationStationId;
    private float destinationLat;
    private float destinationLon;
    private int totalMinutes;
    private int numMinuteRecords;

    public Adjacency(String lineId, String originStationId, String destinationStationId, float destinationLat, float destinationLon) {
        this.lineId = lineId;
        this.originStationId = originStationId;
        this.destinationStationId = destinationStationId;
        this.destinationLat = destinationLat;
        this.destinationLon = destinationLon;
        totalMinutes = 0;
        numMinuteRecords = 0;
    }

    public void addMinuteRecord(int minutes) {
        totalMinutes += minutes;
        numMinuteRecords++;
    }

    public float getAvgMinutes() {
        float avgMinutes = (float) totalMinutes / numMinuteRecords;
        if (avgMinutes == 0) {
            return 0.1F;
        }
        return avgMinutes;
    }

    public String getLineId() {
        return lineId;
    }

    public String getOriginStationId() {
        return originStationId;
    }

    public String getDestinationStationId() {
        return destinationStationId;
    }

    public float getDestinationLat() {
        return destinationLat;
    }

    public float getDestinationLon() {
        return destinationLon;
    }
}
