package com.example.wirelength.service;

import com.example.wirelength.model.CalibrationConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service simple en mémoire pour gérer les calibrations.
 * - Stocke une liste volatile (non persistante) de CalibrationConfig
 * - Fournit création, activation, suppression, conversion
 * - Initialise une calibration par défaut au démarrage
 *
 * Note: pour un usage production, remplacez par un repository (JPA) persistant.
 */
@Service
public class CalibrationService {

    private final List<CalibrationConfig> calibrations = new ArrayList<>();
    private CalibrationConfig activeCalibration;
    private boolean initialized = false;

    public CalibrationService() {
        System.out.println("✅ CalibrationService initialisé");
        initDefaultCalibration();
        initialized = true;
    }

    private void ensureInitialized() {
        if (!initialized) {
            initDefaultCalibration();
            initialized = true;
        }
    }

    private void initDefaultCalibration() {
        if (calibrations.isEmpty()) {
            System.out.println("🎯 Création d'une calibration par défaut...");
            CalibrationConfig defaultCalibration = new CalibrationConfig(
                "Calibration par défaut",
                1.0, // 1 mm/px par défaut
                "Configuration par défaut",
                "Calibration créée automatiquement au démarrage"
            );
            defaultCalibration.setActive(true);
            // Si votre modèle a createdAt
            try {
                defaultCalibration.setCreatedAt(new Date());
            } catch (Exception ignored) {}
            calibrations.add(defaultCalibration);
            activeCalibration = defaultCalibration;
            System.out.println("✅ Calibration par défaut créée: " + defaultCalibration.getName());
        }
        System.out.println("📊 Calibrations disponibles: " + calibrations.size());
    }

    /**
     * Crée une calibration et l’active immédiatement.
     * ratio mm/px = knownWidthMm / measuredPixels
     * cameraSetup: dépend du contexte d’appel (ici on met un libellé générique, surchargez au besoin)
     */
    public CalibrationConfig createCalibration(String name, double knownWidthMm, double measuredPixels, String description) {
        ensureInitialized();

        System.out.println("🎯 Création d'une nouvelle calibration...");
        System.out.println("📝 Paramètres: nom=" + name + ", largeur=" + knownWidthMm + "mm, pixels=" + measuredPixels);

        // Validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la calibration est requis");
        }
        if (knownWidthMm <= 0 || measuredPixels <= 0) {
            throw new IllegalArgumentException("Les valeurs de mesure doivent être positives");
        }

        // Unicité du nom
        Optional<CalibrationConfig> existing = calibrations.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst();
        if (existing.isPresent()) {
            System.out.println("❌ Une calibration avec ce nom existe déjà: " + name);
            throw new IllegalArgumentException("Une calibration avec ce nom existe déjà");
        }

        // Calcul ratio
        double ratio = knownWidthMm / measuredPixels;

        CalibrationConfig config = new CalibrationConfig(
            name,
            ratio,
            // cameraSetup: laissez un indicateur générique; le contrôleur peut créer des descriptions plus précises
            "Calibration manuelle",
            description != null ? description : "Calibration créée manuellement"
        );
        try {
            config.setCreatedAt(new Date());
        } catch (Exception ignored) {}

        // Activer cette calibration
        calibrations.forEach(c -> c.setActive(false));
        config.setActive(true);
        activeCalibration = config;
        calibrations.add(config);

        System.out.println("✅ Nouvelle calibration créée:");
        System.out.println("  - Nom: " + config.getName());
        System.out.println("  - Ratio: " + config.getPixelToMmRatio() + " mm/px");
        System.out.println("  - Active: " + config.isActive());
        System.out.println("  - Nombre total de calibrations: " + calibrations.size());

        return config;
    }

    /**
     * Permet de surcharger le champ cameraSetup et/ou description après création si besoin.
     * Utile si vous souhaitez distinguer "Auto couleur" vs "Manuelle" côté UI, tout en
     * gardant un seul point de calcul de ratio.
     */
    public void updateMetadata(String name, String cameraSetup, String description) {
        ensureInitialized();
        CalibrationConfig c = getCalibrationByName(name);
        if (c == null) throw new IllegalArgumentException("Calibration introuvable: " + name);
        if (cameraSetup != null && !cameraSetup.trim().isEmpty()) {
            c.setCameraSetup(cameraSetup.trim());
        }
        if (description != null && !description.trim().isEmpty()) {
            c.setDescription(description.trim());
        }
    }

    public boolean setActiveCalibration(String name) {
        ensureInitialized();

        System.out.println("🔄 Changement de calibration active: " + name);

        Optional<CalibrationConfig> newActive = calibrations.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst();

        if (newActive.isPresent()) {
            calibrations.forEach(c -> c.setActive(false));
            CalibrationConfig config = newActive.get();
            config.setActive(true);
            activeCalibration = config;
            System.out.println("✅ Calibration active changée pour: " + name);
            return true;
        }

        System.out.println("❌ Calibration non trouvée: " + name);
        return false;
    }

    public CalibrationConfig getActiveCalibration() {
        ensureInitialized();

        if (activeCalibration == null && !calibrations.isEmpty()) {
            activeCalibration = calibrations.get(0);
            activeCalibration.setActive(true);
            System.out.println("⚠️ Aucune calibration active, utilisation de la première disponible: " + activeCalibration.getName());
        }
        return activeCalibration;
    }

    public List<CalibrationConfig> getAllCalibrations() {
        ensureInitialized();

        System.out.println("📋 Liste des calibrations (" + calibrations.size() + "):");
        calibrations.forEach(c -> System.out.println("  - " + c.getName() + (c.isActive() ? " (active)" : "")));
        // renvoyer une copie défensive
        return new ArrayList<>(calibrations);
    }

    public double convertPixelsToMm(double pixels) {
        ensureInitialized();

        CalibrationConfig calibration = getActiveCalibration();
        if (calibration == null) {
            throw new IllegalStateException("Aucune calibration active");
        }
        double result = pixels * calibration.getPixelToMmRatio();
        System.out.println("🔄 Conversion: " + pixels + "px = " + String.format("%.2f", result) + "mm (ratio: " + calibration.getPixelToMmRatio() + ")");
        return result;
    }

    public boolean deleteCalibration(String name) {
        ensureInitialized();

        System.out.println("🗑️ Tentative de suppression de la calibration: " + name);

        if (calibrations.size() <= 1) {
            System.out.println("❌ Impossible de supprimer la dernière calibration");
            return false;
        }

        Optional<CalibrationConfig> toDelete = calibrations.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst();

        if (toDelete.isPresent()) {
            CalibrationConfig config = toDelete.get();
            boolean wasActive = config.isActive();
            calibrations.remove(config);

            if (wasActive) {
                if (!calibrations.isEmpty()) {
                    calibrations.get(0).setActive(true);
                    activeCalibration = calibrations.get(0);
                    System.out.println("ℹ️ Nouvelle calibration active: " + activeCalibration.getName());
                } else {
                    activeCalibration = null;
                }
            }

            System.out.println("✅ Calibration supprimée: " + name);
            return true;
        }

        System.out.println("❌ Calibration non trouvée: " + name);
        return false;
    }

    public CalibrationConfig getCalibrationByName(String name) {
        ensureInitialized();

        return calibrations.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
}