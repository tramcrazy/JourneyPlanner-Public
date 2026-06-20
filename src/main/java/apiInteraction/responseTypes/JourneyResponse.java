package apiInteraction.responseTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JourneyResponse {
    private Journey[] journeys;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Journey {
        private Leg[] legs;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Leg {
            private int duration;
            private Mode mode;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Mode {
                private String id;

                public String getId() {
                    return id;
                }
            }

            public int getDuration() {
                return duration;
            }

            public Mode getMode() {
                return mode;
            }
        }

        public Leg[] getLegs() {
            return legs;
        }
    }

    public Journey[] getJourneys() {
        return journeys;
    }
}
