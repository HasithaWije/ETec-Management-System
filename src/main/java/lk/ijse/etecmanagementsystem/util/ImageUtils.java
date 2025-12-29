package lk.ijse.etecmanagementsystem.util;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {
    static {
        setupImagesDirectoryPath();
    }
    private static String imagesDirectoryPath;

    public static void resizeAndSave(File sourceFile, String outputDirPath, String newFileName, int targetWidth, int targetHeight) throws IOException {
        // 1. Read the image from the source file
        BufferedImage originalImage = ImageIO.read(sourceFile);

        // 2. Create a new empty image buffer with the target dimensions
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());

        // 3. Scale the image using Graphics2D (High Quality)
        Graphics2D g = resizedImage.createGraphics();

        // Settings for high-quality resizing
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the original image onto the new resized buffer
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        // 4. Create the Output Directory if it doesn't exist
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            boolean s = outputDir.mkdirs();
        }

        // 5. Construct the new file (Rename)
        // Ensure we keep the extension or define one (e.g., "png" or "jpg")
        File outputFile = new File(outputDir, newFileName + ".png");

        // 6. Save the new image
        ImageIO.write(resizedImage, "png", outputFile);

        System.out.println("Image saved successfully to: " + outputFile.getAbsolutePath());
    }

    public static void setupImagesDirectoryPath() {

        String userHome = System.getProperty("user.home");

        // Combine it with the rest of your path
        // Result: C:\Users\USER\Documents\ETec Management System\images\
        String destinationPath = userHome
                + File.separator + "Documents" + File.separator + "ETec Management System"
                + File.separator + "images" + File.separator;

        File outputDir = new File(destinationPath);
        if (!outputDir.exists()) {
            boolean s = outputDir.mkdirs();
        }

        imagesDirectoryPath = destinationPath;
    }
    public static String getImagesDirectoryPath() {
        return imagesDirectoryPath;
    }
}