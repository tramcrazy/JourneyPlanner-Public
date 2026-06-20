package webServer;

public class DisruptionContainer {
    public String dots;
    public String descriptions;

    public DisruptionContainer(String[] disruption) {
        dots = disruption[0];
        descriptions = disruption[1];
    }
}
