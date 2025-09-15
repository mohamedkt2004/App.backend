package com.example.wirelength.controller;

import com.example.wirelength.model.CalibrationConfig;
import com.example.wirelength.service.CalibrationService;
import com.example.wirelength.service.ImageAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wirelength")
@CrossOrigin(
    origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = "*",
    allowCredentials = "false"
)
public class WireLengthController {

    @Autowired
    private ImageAnalysisService imageAnalysisService;

    @Autowired
    private CalibrationService calibrationService;

    // Modèles simples
    public static class FabricMeasurementResult {
        private double lengthInPixels;
        private double lengthInMm;
        private double lengthInCm;
        private double lengthInM;
        private String calibrationUsed;
        private double pixelRatio;
        private String measurementDate;
        private ImageSize imageSize;
        private BBox bbox;

        public double getLengthInPixels() { return lengthInPixels; }
        public void setLengthInPixels(double lengthInPixels) { this.lengthInPixels = lengthInPixels; }
        public double getLengthInMm() { return lengthInMm; }
        public void setLengthInMm(double lengthInMm) { this.lengthInMm = lengthInMm; }
        public double getLengthInCm() { return lengthInCm; }
        public void setLengthInCm(double lengthInCm) { this.lengthInCm = lengthInCm; }
        public double getLengthInM() { return lengthInM; }
        public void setLengthInM(double lengthInM) { this.lengthInM = lengthInM; }
        public String getCalibrationUsed() { return calibrationUsed; }
        public void setCalibrationUsed(String calibrationUsed) { this.calibrationUsed = calibrationUsed; }
        public double getPixelRatio() { return pixelRatio; }
        public void setPixelRatio(double pixelRatio) { this.pixelRatio = pixelRatio; }
        public String getMeasurementDate() { return measurementDate; }
        public void setMeasurementDate(String measurementDate) { this.measurementDate = measurementDate; }
        public ImageSize getImageSize() { return imageSize; }
        public void setImageSize(ImageSize imageSize) { this.imageSize = imageSize; }
        public BBox getBbox() { return bbox; }
        public void setBbox(BBox bbox) { this.bbox = bbox; }
    }

    public static class ImageSize {
        private int width;
        private int height;
        public ImageSize() {}
        public ImageSize(int width, int height) { this.width = width; this.height = height; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
    }

    public static class BBox {
        private int x, y, w, h;
        public BBox() {}
        public BBox(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        public int getW() { return w; }
        public void setW(int w) { this.w = w; }
        public int getH() { return h; }
        public void setH(int h) { this.h = h; }
    }

    // ---- CALIBRATION ----

    @PostMapping("/calibrate-auto-color")
    public ResponseEntity<?> createCalibrationAutoColor(
            @RequestParam("file") MultipartFile file,
            @RequestParam("knownWidthMm") double knownWidthMm,
            @RequestParam("configName") String configName,
            @RequestParam(value = "clientDetectedPixels", required = false) Double clientDetectedPixels,
            @RequestParam(value = "r", required = false) Integer r,
            @RequestParam(value = "g", required = false) Integer g,
            @RequestParam(value = "b", required = false) Integer b,
            @RequestParam(value = "tolerance", required = false) Integer tolerance,
            @RequestParam(value = "strategy", required = false) String strategy,
            @RequestParam(value = "bbox", required = false) String bbox
    ) {
        try {
            ImageAnalysisService.AutoAnalysis serverDet =
                    imageAnalysisService.analyzeFabricAuto(file, r, g, b, tolerance);

            if (!serverDet.success || serverDet.pixelLength <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Détection auto impossible pour calibration"));
            }

            double pixels = serverDet.pixelLength;
            CalibrationConfig config = calibrationService.createCalibration(
                    configName.trim(),
                    knownWidthMm,
                    pixels,
                    "Calibration auto-couleur (long côté, tol=" + (tolerance != null ? tolerance : 100) + ", strat=rectangle)"
            );
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- MESURE AUTOMATIQUE ----

    @PostMapping("/measure-fabric-auto")
    public ResponseEntity<?> measureFabricAuto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "r", required = false) Integer r,
            @RequestParam(value = "g", required = false) Integer g,
            @RequestParam(value = "b", required = false) Integer b,
            @RequestParam(value = "tolerance", required = false) Integer tolerance
    ) {
        try {
            CalibrationConfig activeCalibration = calibrationService.getActiveCalibration();
            if (activeCalibration == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucune calibration active"));
            }

            ImageAnalysisService.AutoAnalysis analysis =
                    imageAnalysisService.analyzeFabricAuto(file, r, g, b, tolerance);

            if (!analysis.success || analysis.pixelLength <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun objet coloré détecté"));
            }

            double px = analysis.pixelLength;
            double mm = px * activeCalibration.getPixelToMmRatio();
            double cm = mm / 10.0;
            double m = mm / 1000.0;

            FabricMeasurementResult result = new FabricMeasurementResult();
            result.setLengthInPixels(Math.round(px));
            result.setLengthInMm(Math.round(mm * 100.0) / 100.0);
            result.setLengthInCm(Math.round(cm * 10.0) / 10.0);
            result.setLengthInM(Math.round(m * 1000.0) / 1000.0);
            result.setCalibrationUsed(activeCalibration.getName());
            result.setPixelRatio(activeCalibration.getPixelToMmRatio());
            result.setMeasurementDate(new java.util.Date().toString());
            result.setImageSize(new ImageSize(analysis.imageWidth, analysis.imageHeight));

            if (analysis.bbox != null) {
                BBox bb = new BBox(
                        analysis.bbox.get("x"),
                        analysis.bbox.get("y"),
                        analysis.bbox.get("w"),
                        analysis.bbox.get("h")
                );
                result.setBbox(bb);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Endpoints existants utiles (non modifiés) ----

    @GetMapping("/calibrations")
    public ResponseEntity<?> getCalibrations() {
        List<CalibrationConfig> calibrations = calibrationService.getAllCalibrations();
        return ResponseEntity.ok(calibrations);
    }

    @PostMapping("/set-calibration")
    public ResponseEntity<?> setActiveCalibration(@RequestParam("name") String name) {
        boolean ok = calibrationService.setActiveCalibration(name);
        if (ok) return ResponseEntity.ok(Map.of("active", name));
        return ResponseEntity.badRequest().body(Map.of("error", "Calibration introuvable"));
    }

    @GetMapping("/active-calibration")
    public ResponseEntity<?> getActiveCalibration() {
        CalibrationConfig active = calibrationService.getActiveCalibration();
        return ResponseEntity.ok(active != null ? active : Map.of("message", "Aucune calibration active"));
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        CalibrationConfig active = calibrationService.getActiveCalibration();
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "activeCalibration", active != null ? active.getName() : "aucune"
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<?> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "Wire Length Calculator",
                "version", "2.2.0",
                "endpoints", new String[]{
                        "/health", "/info",
                        "/calibrate-auto-color",
                        "/calibrations", "/set-calibration", "/active-calibration",
                        "/measure-fabric-auto"
                }
        ));
    }

    // ---- Nouveau endpoint: suppression d'une calibration ----

    @DeleteMapping("/calibrations/{name}")
    public ResponseEntity<?> deleteCalibration(@PathVariable("name") String name) {
        try {
            boolean ok = calibrationService.deleteCalibration(name);
            if (ok) {
                return ResponseEntity.ok(Map.of("deleted", name));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Suppression impossible (dernière calibration ou introuvable)"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}