package webServer;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class HelperLibrary {
    public static String generateHttpTime() {
        // Generates a timestamp in the HTTP response style (RFC2616)
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formattedDateTime = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss");
        return currentDateTime.format(formattedDateTime) + " GMT";
    }

    public static String chooseMessage(int responseCode) {
        return switch (responseCode) {
            // Informational 1xx
            case 100 -> "Continue";
            case 101 -> "Switching Protocols";
            case 102 -> "Processing";
            case 103 -> "Early Hints";
            // Success 2xx
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 203 -> "Non-Authoritative Information";
            case 204 -> "No Content";
            case 205 -> "Reset Content";
            case 206 -> "Partial Content";
            case 207 -> "Multi-Status";
            case 208 -> "Already Reported";
            case 226 -> "IM Used";
            // Redirections 3xx
            case 300 -> "Multiple Choices";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 303 -> "See Other";
            case 304 -> "Not Modified";
            case 305 -> "Use Proxy";
            case 306 -> "Switch Proxy";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            // Client side errors 4xx
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 402 -> "Payment Required";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 406 -> "Not Acceptable";
            case 407 -> "Proxy Authentication Required";
            case 408 -> "Request Timeout";
            case 409 -> "Conflict";
            case 410 -> "Gone";
            case 411 -> "Length Required";
            case 412 -> "Precondition Failed";
            case 413 -> "Payload Too Large";
            case 414 -> "URI Too Long";
            case 415 -> "Unsupported Media Type";
            case 416 -> "Range Not Satisfiable";
            case 417 -> "Expectation Failed";
            case 418 -> "I'm a teapot";
            case 421 -> "Misdirected Request";
            case 422 -> "Unprocessable Content";
            case 423 -> "Locked";
            case 424 -> "Failed Dependency";
            case 425 -> "Too Early";
            case 426 -> "Upgrade Required";
            case 428 -> "Precondition Required";
            case 429 -> "Too Many Requests";
            case 431 -> "Request Header Fields Too Large";
            case 451 -> "Unavailable For Legal Reasons";
            // Server side errors 5xx
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            case 505 -> "HTTP Version Not Supported";
            case 506 -> "Variant Also Negotiates";
            case 507 -> "Insufficient Storage";
            case 508 -> "Loop Detected";
            case 510 -> "Not Extended";
            case 511 -> "Network Authentication Required";
            default ->
                    throw new RuntimeException("Error - HTTP response code " + responseCode + " is an invalid code.");
        };
    }

    public static byte[] generateHttpResponse(int responseCode, byte[] body, String contentType, int contentLength) {
        byte[] headers = """
                HTTP/1.1 %d %s
                Date: %s
                Content-Type: %s
                Content-Length: %d
                Server: tortoise/0.1 (%s)
                Connection: close
                
                """.formatted(
                        responseCode,
                        chooseMessage(responseCode),
                        generateHttpTime(),
                        contentType,
                        contentLength,
                        System.getProperty("os.name")
        ).getBytes();
        byte[] finalArray = Arrays.copyOf(headers, headers.length + body.length);
        System.arraycopy(body, 0, finalArray, headers.length, body.length);
        return finalArray;
    }

    public static String[] generateDisruptions(String[][] disruptedLines) {
        if (disruptedLines.length == 0) {
            return new String[]{"", "No disruptions"};
        }

        StringBuilder dotsBuilder = new StringBuilder();
        StringBuilder namesBuilder = new StringBuilder("Disruptions on: ");

        for (String[] disruptedLine : disruptedLines) {
            dotsBuilder.append("<span class=\"span-dot bg-colour-%s\"></span>\n            ".formatted(disruptedLine[0]));
            namesBuilder.append("<b>%s</b>, ".formatted(disruptedLine[1]));
        }

        namesBuilder.delete(namesBuilder.length() - 2, namesBuilder.length() - 1);

        return new String[]{dotsBuilder.toString(), namesBuilder.toString()};
    }
}
