Description
Le backend est une application Spring Boot qui fournit une API REST pour l'analyse d'images de rouleaux de fil et la gestion des calibrations.
Il utilise OpenCV 4.9.0 pour détecter automatiquement des rectangles colorés dans les images et estimer leur longueur en pixels, puis convertir 
ces mesures en unités réelles (mm, cm, m) à l'aide de calibrations. Les calibrations sont gérées en mémoire (non persistant) avec une calibration 
par défaut initialisée au démarrage.


Fonctionnalités principales

Analyse d'image : Détection de rectangles colorés via seuillage RGB et BFS, avec estimation de la longueur via le grand côté ou une analyse PCA.
Gestion des calibrations : Création, activation, suppression et récupération des calibrations (ratio pixel/mm).
Endpoints REST : API pour calibrer, analyser des images, et gérer les calibrations.
Intégration OpenCV : Traitement d'image robuste pour la segmentation et la mesure.

Prérequis

JDK 17 ou supérieur : Pour exécuter l'application Spring Boot.
Maven : Pour gérer les dépendances.
OpenCV 4.9.0 : Pour le traitement d'image.

Installation :
Cloner le dépôt


bash
git clone https://github.com/votre-utilisateur/votre-repo.git  # Remplacez par l'URL de votre repo GitHub
cd wire-length-backend
bash
Configurer les dépendances
Assurez-vous que pom.xml inclut OpenCV :
xml<dependency>
    <groupId>org.openpencv</groupId>
    <artifactId>opencv</artifactId>
    <version>4.9.0-0</version>
</dependency>
Téléchargez les dépendances avec Maven :
bashmvn clean install
Configurer OpenCV

Téléchargez la bibliothèque native OpenCV depuis opencv.org.
Placez le fichier natif (libopencv_java490.so pour Linux/macOS ou opencv_java490.dll pour Windows) dans un dossier lib/.
Configurez -Djava.library.path=/chemin/vers/lib dans votre environnement ou IDE.
Assurez-vous que WirelengthApplication.java charge la bibliothèque :

javastatic {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
}
Lancer le serveur
bashmvn spring-boot:run
Le serveur sera accessible sur http://localhost:8080.
