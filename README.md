# 📡 FiberMeter Backend

Le backend est une application **Spring Boot** qui fournit une **API REST** pour :
* l'analyse d'images de rouleaux de fil,
* et la gestion des calibrations.

Il utilise **OpenCV 4.9.0** pour détecter automatiquement des rectangles colorés dans les images et estimer leur longueur en pixels, puis convertir ces mesures en unités réelles (**mm, cm, m**) à l'aide de calibrations. Les calibrations sont gérées **en mémoire** (non persistantes) avec une calibration par défaut initialisée au démarrage.

## ✨ Fonctionnalités principales

* 🔍 **Analyse d'image** : Détection de rectangles colorés via seuillage RGB et BFS, avec estimation de la longueur via le grand côté ou une analyse PCA.
* ⚖️ **Gestion des calibrations** : Création, activation, suppression et récupération des calibrations (ratio pixel/mm).
* 🌐 **Endpoints REST** : API pour calibrer, analyser des images, et gérer les calibrations.
* 🖼 **Intégration OpenCV** : Traitement d'image robuste pour la segmentation et la mesure.

## ✅ Prérequis

* ☕ **JDK 17 ou supérieur** (pour exécuter Spring Boot)
* 🛠 **Maven** (pour gérer les dépendances)
* 📷 **OpenCV 4.9.0** (pour le traitement d'image)

## ⚙️ Installation

### 1️⃣ Cloner le dépôt

```bash
git clone https://github.com/mohamedkt2004/App.backend.git
cd App.backend
```

### 2️⃣ Configurer les dépendances

Vérifiez que votre `pom.xml` inclut bien OpenCV :

```xml
<dependency>
    <groupId>org.openpencv</groupId>
    <artifactId>opencv</artifactId>
    <version>4.9.0-0</version>
</dependency>
```

Installez les dépendances avec Maven :

```bash
mvn clean install
```

### 3️⃣ Configurer OpenCV

1. Téléchargez la bibliothèque native OpenCV 4.9.0 depuis [opencv.org](https://opencv.org).

2. Placez le fichier natif dans un dossier `lib/` :
   - `libopencv_java490.so` → Linux/macOS
   - `opencv_java490.dll` → Windows

3. Configurez le chemin dans votre environnement ou IDE :
   ```bash
   -Djava.library.path=/chemin/vers/lib
   ```

4. Assurez-vous que `WirelengthApplication.java` charge bien la bibliothèque :
   ```java
   static {
       System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
   }
   ```

## 🚀 Lancer le serveur

```bash
mvn spring-boot:run
```

Le serveur sera accessible à l'adresse :
👉 **http://localhost:8080**

