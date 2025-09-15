package com.example.wirelength.service;

import com.example.wirelength.model.AnalysisResult;
import com.example.wirelength.model.CalibrationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class ImageAnalysisService {

    @Autowired
    private CalibrationService calibrationService;

    public AnalysisResult analyzeImage(MultipartFile file, double barDiameterMm) throws IOException {
        AnalysisResult result = new AnalysisResult();
        result.setSuccess(true);
        result.setMessage("Analyse avec barre de référence completée");
        result.setAnalysisType("bar_reference");
        result.setImageName(file.getOriginalFilename());
        return result;
    }

    public AnalysisResult analyzeImageWithCalibration(MultipartFile file) throws IOException {
        CalibrationConfig activeCalibration = calibrationService.getActiveCalibration();
        if (activeCalibration == null) {
            throw new IllegalStateException("Aucune calibration active");
        }
        AnalysisResult result = new AnalysisResult();
        result.setSuccess(true);
        result.setMessage("Analyse avec calibration completée");
        result.setAnalysisType("calibration");
        result.setCalibrationUsed(activeCalibration.getName());
        result.setPixelRatio(activeCalibration.getPixelToMmRatio());
        result.setImageName(file.getOriginalFilename());
        return result;
    }

    public CalibrationConfig calibrateFromReference(MultipartFile file, double knownWidthMm, String configName) throws IOException {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) throw new IllegalArgumentException("Impossible de lire l'image");
            double referencePixelWidth = image.getWidth();
            CalibrationConfig config = calibrationService.createCalibration(
                configName,
                knownWidthMm,
                referencePixelWidth,
                "Calibration créée automatiquement à partir de l'image " + file.getOriginalFilename()
            );
            return config;

        } catch (IOException e) {
            throw new IOException("Erreur lors de l'analyse de l'image: " + e.getMessage());
        }
    }

    public int[] getImageDimensions(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        if (image != null) {
            return new int[]{image.getWidth(), image.getHeight()};
        }
        throw new IOException("Image non valide");
    }

    public boolean isValidImageFormat(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/") &&
               (contentType.contains("jpeg") || contentType.contains("jpg") ||
                contentType.contains("png") || contentType.contains("bmp"));
    }

    // --------- Détection auto par couleur (utilise le long côté) ----------

    public static class AutoColorDetectionResult {
        private final boolean success;
        private final int pixelLength; // long côté en pixels
        private final Map<String, Integer> bbox; // x,y,w,h

        public AutoColorDetectionResult(boolean success, int pixelLength, Map<String, Integer> bbox) {
            this.success = success;
            this.pixelLength = pixelLength;
            this.bbox = bbox;
        }
        public boolean isSuccess() { return success; }
        public int getPixelLength() { return pixelLength; }
        public Map<String, Integer> getBbox() { return bbox; }
    }

    /**
     * Détecte la composante couleur près du centre, construit son bbox et renvoie
     * la longueur en pixels basée sur le long côté du rectangle détecté.
     * Optionnel: r,g,b,tolerance. Si r/g/b sont nuls, on échantillonne au centre.
     */
    public AutoColorDetectionResult detectLongSideByColor(MultipartFile file, Integer r, Integer g, Integer b, Integer tolerance) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        if (image == null) throw new IOException("Impossible de lire l'image");

        final int width = image.getWidth();
        final int height = image.getHeight();
        final int tol = (tolerance != null && tolerance > 0) ? tolerance : 100;

        // Déterminer cible couleur (centre si non fournie)
        int targetR, targetG, targetB;
        if (r == null || g == null || b == null) {
            int cx = width / 2;
            int cy = height / 2;
            int argb = image.getRGB(cx, cy);
            targetR = (argb >> 16) & 0xFF;
            targetG = (argb >> 8) & 0xFF;
            targetB = (argb) & 0xFF;
        } else {
            targetR = r; targetG = g; targetB = b;
        }

        // Construire un masque simple RGB euclidien
        boolean[] mask = new boolean[width * height];
        int[] rgbRow = new int[width];
        for (int y = 0; y < height; y++) {
            image.getRGB(0, y, width, 1, rgbRow, 0, width);
            for (int x = 0; x < width; x++) {
                int argb = rgbRow[x];
                int rr = (argb >> 16) & 0xFF;
                int gg = (argb >> 8) & 0xFF;
                int bb = (argb) & 0xFF;
                int dr = rr - targetR;
                int dg = gg - targetG;
                int dbb = bb - targetB;
                double dist = Math.sqrt(dr * dr + dg * dg + dbb * dbb);
                mask[y * width + x] = dist <= tol;
            }
        }

        // Trouver composante contenant (ou proche de) centre
        int cx = width / 2;
        int cy = height / 2;
        int startIdx = cy * width + cx;
        if (!mask[startIdx]) {
            int found = -1;
            int R = Math.max(5, Math.min(width, height) / 50);
            outer:
            for (int rrad = 1; rrad <= R; rrad++) {
                for (int dy = -rrad; dy <= rrad; dy++) {
                    for (int dx = -rrad; dx <= rrad; dx++) {
                        int xx = cx + dx, yy = cy + dy;
                        if (xx >= 0 && xx < width && yy >= 0 && yy < height) {
                            int idx = yy * width + xx;
                            if (mask[idx]) { found = idx; break outer; }
                        }
                    }
                }
            }
            startIdx = found;
        }
        if (startIdx < 0) return new AutoColorDetectionResult(false, 0, null);

        // BFS composante
        boolean[] visited = new boolean[width * height];
        Deque<Integer> dq = new ArrayDeque<>();
        dq.add(startIdx);
        visited[startIdx] = true;
        List<int[]> compPts = new ArrayList<>();
        int minx = width, miny = height, maxx = 0, maxy = 0;

        while (!dq.isEmpty()) {
            int idx = dq.removeFirst();
            int x = idx % width;
            int y = idx / width;
            compPts.add(new int[]{x, y});
            if (x < minx) minx = x;
            if (x > maxx) maxx = x;
            if (y < miny) miny = y;
            if (y > maxy) maxy = y;

            int[] neigh = { idx - 1, idx + 1, idx - width, idx + width };
            for (int n : neigh) {
                if (n < 0 || n >= width * height) continue;
                if (visited[n]) continue;
                if (mask[n]) {
                    visited[n] = true;
                    dq.add(n);
                }
            }
        }
        if (compPts.isEmpty()) return new AutoColorDetectionResult(false, 0, null);

        // BBox
        int bboxW = Math.max(1, maxx - minx + 1);
        int bboxH = Math.max(1, maxy - miny + 1);

        // Long côté = max(bboxW, bboxH)
        int longSideByBbox = Math.max(bboxW, bboxH);

        // Optionnel: raffiner via PCA (approx "oriented length")
        int longSideByPCA = estimateMajorAxisLength(compPts);

        int finalPx = Math.max(longSideByBbox, longSideByPCA);

        Map<String, Integer> bbox = new HashMap<>();
        bbox.put("x", minx); bbox.put("y", miny);
        bbox.put("w", bboxW); bbox.put("h", bboxH);

        return new AutoColorDetectionResult(true, finalPx, bbox);
    }

    /**
     * Estime la longueur du grand axe via PCA 2D simple (projection sur la 1ère composante).
     * Retourne un diamètre approximatif en pixels (max - min sur axe principal).
     */
    private int estimateMajorAxisLength(List<int[]> points) {
        int n = points.size();
        if (n < 2) return 0;

        double meanX = 0, meanY = 0;
        for (int[] p : points) { meanX += p[0]; meanY += p[1]; }
        meanX /= n; meanY /= n;

        double sxx = 0, sxy = 0, syy = 0;
        for (int[] p : points) {
            double dx = p[0] - meanX;
            double dy = p[1] - meanY;
            sxx += dx * dx;
            sxy += dx * dy;
            syy += dy * dy;
        }
        sxx /= n; sxy /= n; syy /= n;

        // valeurs propres de [[sxx, sxy],[sxy, syy]]
        double trace = sxx + syy;
        double det = sxx * syy - sxy * sxy;
        double tmp = Math.sqrt(Math.max(0, trace * trace - 4 * det));
        double lambda1 = (trace + tmp) / 2.0; // max
        // vecteur propre associé: (v1, 1) si sxy != 0; sinon (1,0) ou (0,1)
        double vx, vy;
        if (Math.abs(sxy) > 1e-9) {
            vx = lambda1 - syy;
            vy = sxy;
        } else {
            if (sxx >= syy) { vx = 1; vy = 0; } else { vx = 0; vy = 1; }
        }
        double norm = Math.hypot(vx, vy);
        if (norm < 1e-9) return 0;
        vx /= norm; vy /= norm;

        double minProj = Double.POSITIVE_INFINITY;
        double maxProj = Double.NEGATIVE_INFINITY;
        for (int[] p : points) {
            double dx = p[0] - meanX;
            double dy = p[1] - meanY;
            double proj = dx * vx + dy * vy;
            if (proj < minProj) minProj = proj;
            if (proj > maxProj) maxProj = proj;
        }
        double diameter = maxProj - minProj; // en pixels (projection)
        return (int) Math.round(diameter);
    }

    // --------- Analyse automatique globale ----------

    public static class AutoAnalysis {
        public boolean success;
        public int pixelLength; // long côté
        public Map<String, Integer> bbox;
        public int imageWidth;
        public int imageHeight;
    }

    public AutoAnalysis analyzeFabricAuto(MultipartFile file, Integer r, Integer g, Integer b, Integer tolerance) throws IOException {
        AutoColorDetectionResult det = detectLongSideByColor(file, r, g, b, tolerance);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        if (image == null) throw new IOException("Impossible de lire l'image");

        AutoAnalysis out = new AutoAnalysis();
        out.success = det.isSuccess();
        out.pixelLength = det.getPixelLength();
        out.bbox = det.getBbox();
        out.imageWidth = image.getWidth();
        out.imageHeight = image.getHeight();
        return out;
    }
}