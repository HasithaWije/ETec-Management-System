package lk.ijse.etecmanagementsystem.util;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.awt.Toolkit; // For the Beep
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScanner implements Runnable {

    // --- Singleton Instance ---
    private static BarcodeScanner instance;
    private final TextField targetField;

    // --- Scanning Configuration ---
    private Webcam webcam = null;
    private boolean isRunning = false; // Is the scanner currently active?
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    // --- Logic Variables (Memory & Stability) ---
    private String lastScannedCode = "";
    private String tempCode = "";
    private int sameCodeCount = 0;
    private final int STABILITY_THRESHOLD = 3; // Number of stable frames required

    // --- Reader & Filter Setup ---
    private final MultiFormatReader reader = new MultiFormatReader();

    {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A,
                BarcodeFormat.CODE_128,
                BarcodeFormat.CODE_39
        ));
        reader.setHints(hints);
    }

    // --- Private Constructor (Singleton) ---
    private BarcodeScanner(TextField targetField) {
        this.targetField = targetField;
    }

    // --- Public Access Point (Like your Server) ---
    public static BarcodeScanner getInstance(TextField targetField) {
        if (instance == null) {
            instance = new BarcodeScanner(targetField);
        }
        return instance;
    }

    public void startScan() {
        if (isRunning) return; // Already running

        // 1. Create a NEW executor if the old one is null or dead
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        // 1. Initialize Webcam
        webcam = Webcam.getDefault();
        if (webcam == null) {
            System.out.println("No Webcam Found!");
            return;
        }

        webcam.setViewSize(com.github.sarxos.webcam.WebcamResolution.VGA.getSize());
        webcam.open();

        // 2. Start the Thread
        isRunning = true;
        executor.execute(this);
        System.out.println("Webcam Scanner Started!");
    }


    public void stopScan() {
        // 1. Break the loop
        isRunning = false;

        // 2. Close the camera
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }

        // 3. Kill the Thread Manager (IMPORTANT)
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow(); // Force kill the background thread
        }

        // 3. FORCE RESET (The Fix)
        // We wipe the variable so 'startScan' KNOWS it must create a new one.
        executor = null;
        instance = null;

        System.out.println("Scanner stopped and reset.");
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(40); // Fast scan (~25 FPS)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!webcam.isOpen()) continue;

            BufferedImage image = webcam.getImage();
            if (image == null) continue;

            try {
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                Result result = reader.decode(bitmap);

                // 1. Strictly IGNORE QR Codes
                if (result.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
                    continue;
                }

                String currentText = result.getText();

                // 2. Stability Check (Must match 3 times)
                if (currentText.equals(tempCode)) {
                    sameCodeCount++;
                } else {
                    tempCode = currentText;
                    sameCodeCount = 1;
                }

                if (sameCodeCount == STABILITY_THRESHOLD) {
                    // 3. Check if it's a NEW item (not the one we just scanned)
                    if (!currentText.equals(lastScannedCode)) {

                        lastScannedCode = currentText;

                        System.out.println("CONFIRMED: " + currentText);
                        Toolkit.getDefaultToolkit().beep(); // Sound

                        // 4. Update JavaFX UI (The TextField)
                        Platform.runLater(() -> {
                            targetField.setText(currentText);
                            // If you want to clear the memory so you can scan the same item again immediately:
                            // lastScannedCode = "";
                        });
                    }
                }

            } catch (NotFoundException e) {
                // No barcode found
            }
        }
    }
}