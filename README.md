# 📡 Backend - FiberMeter

Le backend est une application **Spring Boot** qui fournit une **API REST** pour :  
- l'analyse d'images de rouleaux de fil,  
- et la gestion des calibrations.  

Il utilise **OpenCV 4.9.0** pour détecter automatiquement des rectangles colorés dans les images et estimer leur longueur en pixels, puis convertir ces mesures en unités réelles (**mm, cm, m**) à l'aide de calibrations.  
Les calibrations sont gérées **en mémoire** (non persistantes) avec une calibration par défaut initialisée au démarrage.

---

## ✨ Fonctionnalités principales

- 🔍 **Analyse d'image** : Détection de rectangles colorés via seuillage RGB et BFS, avec estimation de la longueur via le grand côté ou une analyse PCA.  
- ⚖️ **Gestion des calibrations** : Création, activation, suppression et récupération des calibrations (ratio pixel/mm).  
- 🌐 **Endpoints REST** : API pour calibrer, analyser des images, et gérer les calibrations.  
- 🖼 **Intégration OpenCV** : Traitement d'image robuste pour la segmentation et la mesure.  

---

## ✅ Prérequis

- ☕ **JDK 17 ou supérieur** (pour exécuter Spring Boot)  
- 🛠 **Maven** (pour gérer les dépendances)  
- 📷 **OpenCV 4.9.0** (pour le traitement d'image)  

---

## ⚙️ Installation

### 1️⃣ Cloner le dépôt
```bash
git clone https://github.com/mohamedkt2004/App.backend.git
cd App.backend
