package webServer;

import webServer.pageHandlers.PageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final HashMap<String, PageHandler> pages;
    private final UUID connectionID = UUID.randomUUID();

    public ClientHandler(Socket socket, HashMap<String, PageHandler> pages) {
        super("ClientHandler");
        this.clientSocket = socket;
        this.pages = pages;

        System.out.println("Connection with ID " + connectionID + " established with host " + clientSocket.getInetAddress());
    }

    public void run() {
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            String inputLine;
            byte[] outputData;
            HttProtocol http = new HttProtocol(pages);

            // Handle requests line by line
            while ((inputLine = inputReader.readLine()) != null) {
                outputData = http.processInput(inputLine);
                if (!(outputData == null)) {
                    outputStream.write(outputData);
                    break;
                }
            }

            clientSocket.close();
            System.out.println("Connection with ID " + connectionID + " closed with host " + clientSocket.getInetAddress());
        } catch (IOException e) {
            System.out.println("An error occurred while trying to handle a request from client " + clientSocket.getInetAddress() + ".");
            e.printStackTrace();
        }
    }
}
