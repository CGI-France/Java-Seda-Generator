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
CONFIG_LOCATION=$2
LOGBACK_DIR=$3
URI=$4
AGREEMENT=$5
ARCHIVE_FOLDER=$6
DATA_FILE=$7
OUT_SUMMARY=$8
OUT_SUMMARY_ERROR=$9

#LIB
ANTLR_LIB=lib/antlr-2.7.7.jar
ANTLR_RUNTIME_LIB=lib/antlr-runtime-3.5.jar
CHEMISTRY_OPENCMIS_CLIENT_API_LIB=lib/chemistry-opencmis-client-api-0.13.0.jar
CHEMISTRY_OPENCMIS_CLIENT_BINDINGS_LIB=lib/chemistry-opencmis-client-bindings-0.13.0.jar
CHEMISTRY_OPENCMIS_CLIENT_IMPL_LIB=lib/chemistry-opencmis-client-impl-0.13.0.jar
CHEMISTRY_OPENCMIS_CLIENT_COMMONS_API_LIB=lib/chemistry-opencmis-commons-api-0.13.0.jar
CHEMISTRY_OPENCMIS_CLIENT_COMMONS_IMPL_LIB=lib/chemistry-opencmis-commons-impl-0.13.0.jar
CHEMISTRY_OPENCMIS_CLIENT_SERVER_SUPPORT_LIB=lib/chemistry-opencmis-server-support-0.13.0.jar
COMMONS_LANG_LIB=lib/commons-lang-2.3.jar
LOGBACK_LIB=lib/logback-classic-1.0.7.jar
LOGBACK_CORE_LIB=lib/logback-core-1.0.7.jar
POSTGRESQL_LIB=lib/postgresql-9.4-1201-jdbc41.jar
SERIALIZER_LIB=lib/serializer-2.7.1.jar
SLF4J_LIB=lib/slf4j-api-1.7.12.jar
STAX2_LIB=lib/stax2-api-3.1.4.jar
STRINGTEMPLATE_LIB=lib/stringtemplate-3.2.1.jar
WOODSTOX_LIB=lib/woodstox-core-asl-4.4.0.jar
XALAN_LIB=lib/xalan-2.7.1.jar
XMLAPIS_LIB=lib/xml-apis-1.3.04.jar
JAVASEDAPROFILEGENERATOR=lib/JavaSedaProfileGenerator-1.1.1.jar

JAVA_BIN=java

CLASSPATH=$ANTLR_LIB:$ANTLR_RUNTIME_LIB:$CHEMISTRY_OPENCMIS_CLIENT_API_LIB:$CHEMISTRY_OPENCMIS_CLIENT_BINDINGS_LIB:$CHEMISTRY_OPENCMIS_CLIENT_IMPL_LIB:$CHEMISTRY_OPENCMIS_CLIENT_COMMONS_API_LIB:$CHEMISTRY_OPENCMIS_CLIENT_COMMONS_IMPL_LIB:$CHEMISTRY_OPENCMIS_CLIENT_SERVER_SUPPORT_LIB:$COMMONS_LANG_LIB:$LOGBACK_LIB:$LOGBACK_CORE_LIB:$POSTGRESQL_LIB:$SERIALIZER_LIB:$SLF4J_LIB:$STAX2_LIB:$STRINGTEMPLATE_LIB:$WOODSTOX_LIB:$XALAN_LIB:$XMLAPIS_LIB:$JAVASEDAPROFILEGENERATOR:$LOGBACK_DIR
$JAVA_BIN -classpath $CLASSPATH unit.SedaGeneratorUnit "$PROPERTIES_LOCATION" "$CONFIG_LOCATION" "$URI" "$AGREEMENT" "$ARCHIVE_FOLDER" "$DATA_FILE" "$OUT_SUMMARY" "$OUT_SUMMARY_ERROR"
