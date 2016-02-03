#! /bin/sh
# Ce script est un exemple de lancement de la génération d'un bordereau.
# PARAMETRES :
# - PROPERTIES_LOCATION : chemin vers un fichier renseignant "DATABASE_URL=" "DATABASE_USER=" et "DATABASE_PASSWD="
# - LOGBACK_DIR : chemin vers le dossier contenant logback.xml
# - URI (Colonne SAE_Serveur de la table SAE): première partie de la clé (uri, agreement) permettant de récupérer le chemin vers le profil en base de données.
# - AGREEMENT (Colonne SAE_AccordVersement de la table SAE) : deuxième partie de la clé (uri, agreement) permettant de récupérer le chemin vers le profil en base de données.
# - ARCHIVE_FOLDER : chemin vers le dossier racine de l'archive (non compressé)
# - DATA_FILE : chemin vers le fichier de données métiers à utiliser pour la génération du bordereau
# - OUT_SUMMARY : chemin vers le bordereau de sortie.
# - OUT_SUMMARY_ERROR : chemin vers le fichier d'erreur à créer, plutôt que le bordereau, si la génération de ce bordereau est en erreur.
# CODES D'ERREUR :
# - 0 : Succès.
# - 1 : Erreur grave, non tracées dans OUT_SUMMARY
# - 2 : Erreurs, tracées dans OUT_SUMMARY

#ARGUMENTS
PROPERTIES_LOCATION=$1
LOGBACK_DIR=$2
URI=$3
AGREEMENT=$4
ARCHIVE_FOLDER=$5
DATA_FILE=$6
OUT_SUMMARY=$7
OUT_SUMMARY_ERROR=$8

#LIB
LOGBACK_LIB=lib/logback-classic-1.0.7.jar
LOGBACK_CORE_LIB=lib/logback-core-1.0.7.jar
POSTGRESQL_LIB=lib/postgresql-9.4-1201-jdbc41.jar
SAXON_LIB=lib/saxon-8.7.jar
SERIALIZER_LIB=lib/serializer-2.7.1.jar
SLF4J_LIB=lib/slf4j-api-1.7.12.jar
XALAN_LIB=lib/xalan-2.7.1.jar
XMLAPIS_LIB=lib/xml-apis-1.3.04.jar
COMMONS_LANG_LIB=lib/commons-lang-2.3.jar
JAVASEDAPROFILEGENERATOR=lib/JavaSedaProfileGenerator-1.0.jar

JAVA_BIN=java

CLASSPATH=$LOGBACK_LIB:$LOGBACK_CORE_LIB:$POSTGRESQL_LIB:$SAXON_LIB:$SERIALIZER_LIB:$SLF4J_LIB:$XALAN_LIB:$XMLAPIS_LIB:$COMMONS_LANG_LIB:$JAVASEDAPROFILEGENERATOR:$LOGBACK_DIR

$JAVA_BIN -classpath $CLASSPATH unit.SedaGeneratorUnit "$PROPERTIES_LOCATION" "$URI" "$AGREEMENT" "$ARCHIVE_FOLDER" "$DATA_FILE" "$OUT_SUMMARY" "$OUT_SUMMARY_ERROR"
