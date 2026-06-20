package apiInteraction.responseTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
    private LineStatus[] lineStatuses;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineStatus {
        private Disruption disruption;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Disruption {
            private String description;
            private String closureText;

            public String getDescription() {
                return description;
            }

            public String getClosureText() {
                return closureText;
            }
        }

        public Disruption getDisruption() {
            return disruption;
        }
    }

    public LineStatus[] getLineStatuses() {
        return lineStatuses;
    }
}
