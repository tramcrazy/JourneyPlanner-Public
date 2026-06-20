package webServer;

public record Stage(String lineId, String firstStationOnLine, String secondStationOnLine, String finalStationOnLine, int numStationsOnLine, float stageTime) {}