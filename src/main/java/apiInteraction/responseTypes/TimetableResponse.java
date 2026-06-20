package apiInteraction.responseTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimetableResponse {
    private String lineId;
    private Station[] stations;
    private Stop[] stops;
    private Timetable timetable;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Station {
        private String status;
        private String id;
        private String name;

        public String getStatus() {
            return status;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stop {
        private String status;
        private String id;
        private String name;
        private double lat;
        private double lon;

        public String getStatus() {
            return status;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Timetable {
        private String departureStopId;
        private Route[] routes;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Route {
            private StationInterval[] stationIntervals;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class StationInterval {
                private String id;
                private Interval[] intervals;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Interval {
                    private String stopId;
                    private double timeToArrival;

                    public String getStopId() {
                        return stopId;
                    }

                    public double getTimeToArrival() {
                        return timeToArrival;
                    }
                }

                public String getId() {
                    return id;
                }

                public Interval[] getIntervals() {
                    return intervals;
                }
            }

            public StationInterval[] getStationIntervals() {
                return stationIntervals;
            }
        }

        public String getDepartureStopId() {
            return departureStopId;
        }

        public Route[] getRoutes() {
            return routes;
        }
    }

    public String getLineId() {
        return lineId;
    }

    public Station[] getStations() {
        return stations;
    }

    public Stop[] getStops() {
        return stops;
    }

    public Timetable getTimetable() {
        return timetable;
    }
}
