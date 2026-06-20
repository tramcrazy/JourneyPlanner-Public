package webServer;

import webServer.pageHandlers.PageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class Server {
    private final int serverPort;
    private final HashMap<String, PageHandler> pages;

    public Server(int serverPort) {
        this.serverPort = serverPort;
        this.pages = new HashMap<>();
    }

    public void registerPage(String url, PageHandler handler) {
        // Note that any pages registered here must use the relative URL format, e.g. /results or similar
        // There is an exception for error pages which MUST be registered using only the appropriate HTTP error code e.g. 404
        pages.put(url, handler);
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                new ClientHandler(serverSocket.accept(), pages).start();
            }
        } catch (IOException e) {
            System.out.println("Error while trying to start tortoise - could not open socket on port " + serverPort);
            e.printStackTrace();
        }
    }
}
