package apiInteraction.responseTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Arrival {
    private String lineId;
    private String destinationNaptanId;
    private int timeToStation;

    public String getLineId() {
        return lineId;
    }

    public String getDestinationNaptanId() {
        return destinationNaptanId;
    }

    public int getTimeToStation() {
        return timeToStation;
    }
}
