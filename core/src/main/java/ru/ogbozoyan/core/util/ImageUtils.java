package ru.ogbozoyan.core.util;

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

public class ImageUtils {
    private static final int TOP_MARGIN = 235;
    private static final int LEFT_MARGIN = 350;
    private static final int RIGHT_MARGIN = 295;
    private static final int BOTTOM_MARGIN = 355;

    public static byte[] parseAndZip(BufferedImage img) throws IOException {
        Map<String, int[]> regions = getRegions(img.getWidth(), img.getHeight());
        Map<String, BufferedImage> croppedImages = cropRegions(img, regions);
        return createZipFromImages(croppedImages);
    }

    private static Map<String, int[]> getRegions(int imgWidth, int imgHeight) {
        final int REF_WIDTH = 2550;
        final int REF_HEIGHT = 3300;

        double scaleX;
        if (imgWidth < REF_WIDTH) {
            scaleX = imgWidth / (double) REF_WIDTH;
        } else {
            scaleX = REF_WIDTH / (double) imgWidth;
        }

        double scaleY;
        if (imgHeight < REF_HEIGHT) {
            scaleY = (double) imgHeight / (double) REF_HEIGHT;
        } else {
            scaleY = (double) REF_HEIGHT / (double) imgHeight;
        }

        Map<String, int[]> regions = new HashMap<>();
        regions.put("table_1", scaleRegion(TOP_MARGIN + 535, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 1950, scaleX, scaleY));
        regions.put("table_1_2", scaleRegion(TOP_MARGIN + 835, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 1770, scaleX, scaleY));
        regions.put("table_2_1", scaleRegion(TOP_MARGIN + 1125, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 1495, scaleX, scaleY));
        regions.put("table_2_2", scaleRegion(TOP_MARGIN + 1275, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 1310, scaleX, scaleY));
        regions.put("table_3_1", scaleRegion(TOP_MARGIN + 1470, LEFT_MARGIN + 50, RIGHT_MARGIN + 262, BOTTOM_MARGIN + 1155, scaleX, scaleY));
        regions.put("table_3_2", scaleRegion(TOP_MARGIN + 1632, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 980, scaleX, scaleY));
        regions.put("table_4_1", scaleRegion(TOP_MARGIN + 1831, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 805, scaleX, scaleY));
        regions.put("table_4_2", scaleRegion(TOP_MARGIN + 1987, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 630, scaleX, scaleY));
        regions.put("table_5_1", scaleRegion(TOP_MARGIN + 2173, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 455, scaleX, scaleY));
        regions.put("table_5_2", scaleRegion(TOP_MARGIN + 2345, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 280, scaleX, scaleY));
        regions.put("last_number", scaleRegion(TOP_MARGIN + 2555, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN + 20, scaleX, scaleY));

        return regions;
    }

    private static int[] scaleRegion(int top, int left, int right, int bottom, double scaleX, double scaleY) {
        return new int[] {
            (int) (top * scaleY),
            (int) (left * scaleX),
            (int) (right * scaleX),
            (int) (bottom * scaleY)
        };
    }

    private static Map<String, BufferedImage> cropRegions(BufferedImage image, Map<String, int[]> regions) {
        Map<String, BufferedImage> cropped = new HashMap<>();
        int h = image.getHeight();
        int w = image.getWidth();

        for (Map.Entry<String, int[]> entry : regions.entrySet()) {
            String name = entry.getKey();
            int[] m = entry.getValue();

            int top = m[0], left = m[1], right = m[2], bottom = m[3];
            int y1 = Math.max(0, top);
            int y2 = Math.max(0, h - bottom);
            int x1 = Math.max(0, left);
            int x2 = Math.max(0, w - right);

            int cropWidth = Math.max(1, x2 - x1);
            int cropHeight = Math.max(1, y2 - y1);

            try {
                BufferedImage region = image.getSubimage(x1, y1, cropWidth, cropHeight);
                cropped.put(name, region);
            } catch (RasterFormatException e) {
                System.err.printf("⚠️ Warning: Failed to crop region '%s'. Skipped.\n", name);
            }
        }
        return cropped;
    }

    private static byte[] createZipFromImages(Map<String, BufferedImage> images) throws IOException {
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(zipBuffer)) {
            for (Map.Entry<String, BufferedImage> entry : images.entrySet()) {
                String fileName = entry.getKey() + ".png";
                ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
                ImageIO.write(entry.getValue(), "png", imageBuffer);

                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.write(imageBuffer.toByteArray());
                zipOut.closeEntry();
            }
        }
        return zipBuffer.toByteArray();
    }

    /**
     * Увеличение контраста через RescaleOp (умножение и смещение)
     *
     * @param image  входное изображение
     * @param scale  коэффициент умножения (напр. 1.5f для 150%)
     * @param offset сдвиг яркости (0 — не трогаем)
     */
    public static BufferedImage adjustContrast(BufferedImage image, float scale, float offset) {
        RescaleOp op = new RescaleOp(scale, offset, null);
        return op.filter(image, null);
    }
}
