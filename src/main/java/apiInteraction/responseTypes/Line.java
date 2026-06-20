package apiInteraction.responseTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Line {
    private String id;
    private String name;
    private RouteSection[] routeSections;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteSection {
        private String name;
        private String originator;
        private String destination;

        public String getName() {
            return name;
        }

        public String getOriginator() {
            return originator;
        }

        public String getDestination() {
            return destination;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RouteSection[] getRouteSections() {
        return routeSections;
    }
}
