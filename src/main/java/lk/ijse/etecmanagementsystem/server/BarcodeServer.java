package lk.ijse.etecmanagementsystem.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class BarcodeServer {

    private HttpServer server;
    // Reference to your JavaFX TextField or Controller where you want the data
    private final TextField targetField;

    private static BarcodeServer barcodeServer;


    private BarcodeServer(TextField targetField) {
        this.targetField = targetField;
    }


    public void startServer() {
        try {
            // Create server on port 8080
            // "0.0.0.0" means listen on ALL network interfaces (Wi-Fi, Ethernet, etc.)
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

            // Create a context (endpoint) called "/scan"
            server.createContext("/scan", new ScanHandler());

            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Barcode Server started on port 8080...");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    // Inner class to handle the incoming HTTP request
    class ScanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. Parse the request URL
            String finalBarcode = getString(exchange);
            Platform.runLater(() -> {
                System.out.println("Received Barcode: " + finalBarcode);
                targetField.setText(finalBarcode);
                // Optional: Trigger a method, e.g., processBarcode(finalBarcode);
            });

            // 3. Send response back to phone
            String response = "Scanned: " + finalBarcode;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

        private static String getString(HttpExchange exchange) {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery(); // returns "code=12345"

            String barcode = "";
            if (query != null && query.contains("code=")) {
                // Simple string parsing to get the value after "code="
                barcode = query.split("code=")[1];
                // Handle cases where there might be more params (optional)
                if (barcode.contains("&")) {
                    barcode = barcode.split("&")[0];
                }
            }

            // 2. Update JavaFX UI (MUST be on JavaFX Application Thread)
            String finalBarcode = barcode;
            return finalBarcode;
        }


    }
    public static BarcodeServer getBarcodeServerInstance(TextField targetField) {
        if(barcodeServer == null) {
            barcodeServer = new BarcodeServer(targetField);
        }

        return barcodeServer;
    }
}
