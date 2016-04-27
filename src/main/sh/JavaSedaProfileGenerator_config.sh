#! /bin/sh
# - Ce script génère :
#     - un bordereau RACINE/bordereaux/bordereau.xml
#     - ou un fichier d'erreur RACINE/traces/bordereaux.log
# - A partir de :
#     - (A DEPOSER) RACINE/documents
#     - (A DEPOSER) RACINE/metier/liste-fichiers.txt
# - Le fichier job.config : 
#     - contient une section [accord-versement : ACCORD] 
#     - pour l'URI http://test
#     - afin d'utiliser le profil : (A DEPOSER) RACINE/profils/profil_schema.rng

# ARGUMENTS PAR DEFAUT
BDD_PROPERTIES="sedaGenerator.properties"
CONFIG_LOCATION="job.config"
LOGBACK_DIR=logback/
URI="http://test"
AGREEMENT="ACCORD"
ARCHIVE_FOLDER=RACINE/documents
DATA_FILE=RACINE/metier/liste-fichiers.txt
OUT_SUMMARY=RACINE/bordereaux/bordereau.xml
OUT_SUMMARY_ERROR=RACINE/traces/bordereaux.log

./JavaSedaProfileGenerator_unit.sh $BDD_PROPERTIES $CONFIG_LOCATION $LOGBACK_DIR $URI $AGREEMENT $ARCHIVE_FOLDER $DATA_FILE $OUT_SUMMARY $OUT_SUMMARY_ERROR
