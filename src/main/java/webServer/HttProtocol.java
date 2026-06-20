package webServer;

import webServer.defaultHandlers.ErrorHandler;
import webServer.pageHandlers.PageHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import static webServer.HelperLibrary.generateHttpResponse;

public class HttProtocol {
    private HttpRequestState requestState;
    private final HashMap<String, PageHandler> pages;
    private PageHandler pageHandler;
    private String url;
    private String[][] queryParam2d;
    private String[] queryParamsArray;

    public HttProtocol(HashMap<String, PageHandler> pages) {
        requestState = HttpRequestState.BLANK;
        this.pages = pages;
        queryParam2d = new String[][]{};
    }

    public byte[] processInput(String requestLine) {
        switch (requestState) {
            case BLANK:
                return parseRequestLine(requestLine);
            case METHOD:
                return handleNormalPageGet();
        }

        throw new RuntimeException("Unexpected enum state encountered when trying to process HTTP request.");
    }

    private byte[] parseRequestLine(String requestLine) {
        String[] splitLine = requestLine.split(" ");
        url = splitLine[1];

        HttpRequestMethod requestMethod;
        if (splitLine[0].equals("GET")) {
            requestMethod = HttpRequestMethod.GET;
        } else {
            requestMethod = HttpRequestMethod.OTHER;
        }

        String[] splitUrl = url.substring(1).split("/");

        // check if we have query params
        String queryParams = splitUrl[splitUrl.length - 1];
        if (!queryParams.isEmpty()) {
            if (queryParams.toCharArray()[0] == '?') {
                queryParams = queryParams.substring(1);
                System.out.println(queryParams + " (encoded)");
                queryParams = URLDecoder.decode(queryParams, StandardCharsets.UTF_8);
                System.out.println(queryParams + " (decoded)");
                formatQueryParamsAndUrl(splitUrl, queryParams);
                createQueryParam2d(queryParamsArray);

            } else if (queryParams.split("\\?").length > 1) {
                String[] splitQueryParams = queryParams.split("\\?");
                queryParams = splitQueryParams[splitQueryParams.length - 1];
                formatQueryParamsAndUrl(splitUrl, queryParams);

                if (!splitQueryParams[0].isEmpty()) {
                    char[] urlCharArray = url.toCharArray();
                    if (urlCharArray[urlCharArray.length - 1] != '/') {
                        url += "/";
                    }

                    url += splitQueryParams[0];
                }

                createQueryParam2d(queryParamsArray);
            }
        }

        if (splitUrl[0].equals("static")) {
            if (requestMethod == HttpRequestMethod.GET) {
                return generateStaticResponse();
            } else {
                return generateError(405, url);
            }
        }

        pageHandler = pages.get(url);

        if (pageHandler == null) {
            return generateError(404, url);
        }

        boolean validMethodForUrl = false;

        for (HttpRequestMethod method : pageHandler.getAllowedMethods()) {
            if (method.equals(requestMethod)) {
                validMethodForUrl = true;
                break;
            }
        }

        if (validMethodForUrl) {
            requestState = HttpRequestState.METHOD;
            return null;
        } else {
            return generateError(405, url);
        }
    }

    private void formatQueryParamsAndUrl(String[] splitUrl, String queryParams) {
        String[] splitQpArray = queryParams.split("&");
        queryParamsArray = new String[splitQpArray.length];
        for (int i = 0; i < queryParamsArray.length; i++) {
            queryParamsArray[i] = URLDecoder.decode(splitQpArray[i], StandardCharsets.UTF_8);
        }
        url = "/" + String.join("/", Arrays.stream(splitUrl, 0, splitUrl.length - 1).toArray(String[]::new));
    }

    private void createQueryParam2d(String[] queryParamsArray) {
        String[] splitParam;
        queryParam2d = new String[queryParamsArray.length][2];

        for (int i = 0; i < queryParamsArray.length; i++) {
            splitParam = queryParamsArray[i].split("=");
            if (splitParam.length >= 1) {
                queryParam2d[i][0] = splitParam[0];
            }

            if (splitParam.length >= 2) {
                queryParam2d[i][1] = splitParam[1];
            }
        }
    }

    private byte[] generateError(int errorCode, String url) {
        PageHandler errorHandler = pages.get(String.valueOf(errorCode));

        if (errorHandler == null) {
            errorHandler = new ErrorHandler(errorCode, url);
        }

        System.out.println("Serving error page for error " + errorCode + " on url " + url);
        return errorHandler.getPage();
    }

    private byte[] generateStaticResponse() {
        byte[] staticFile;
        Path filePath;
        String mimeType;

        try {
            InputStream fileStream = getClass().getResourceAsStream(url);
            assert fileStream != null;
            staticFile = fileStream.readAllBytes();
            filePath = Paths.get(url);
            mimeType = Files.probeContentType(filePath);
        } catch (IOException | InvalidPathException e) {
            return generateError(404, url);
        }

        System.out.println("Serving static file from path " + filePath + " of type " + mimeType);
        return generateHttpResponse(200, staticFile, mimeType, staticFile.length);
    }

    private byte[] handleNormalPageGet() {
        System.out.println("Serving normal page (GET request from browser) for url " + url + " with processed query parameters " + Arrays.deepToString(queryParam2d));
        return pageHandler.getPage(queryParam2d);
    }
}
