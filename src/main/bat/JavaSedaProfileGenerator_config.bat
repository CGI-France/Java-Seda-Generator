@ECHO OFF
REM - Ce script génère :
REM     - un bordereau RACINE/bordereaux/bordereau.xml
REM     - ou un fichier d'erreur RACINE/traces/bordereaux.log
REM - A partir de :
REM     - (A DEPOSER) RACINE/documents
REM     - (A DEPOSER) RACINE/metier/liste-fichiers.txt
REM - Le fichier job.config : 
REM     - contient une section [accord-versement : ACCORD] 
REM     - pour l'URI http://test
REM     - afin d'utiliser le profil : (A DEPOSER) RACINE/profils/profil_schema.rng

REM ARGUMENTS PAR DEFAUT
SET BDD_PROPERTIES="sedaGenerator.properties"
SET CONFIG_LOCATION="job.config"
SET LOGBACK_DIR=logback/
SET URI="http://test"
SET AGREEMENT="ACCORD"
SET ARCHIVE_FOLDER=RACINE/documents
SET DATA_FILE=RACINE/metier/liste-fichiers.txt
SET OUT_SUMMARY=RACINE/bordereaux/bordereau.xml
SET OUT_SUMMARY_ERROR=RACINE/traces/bordereaux.log

call JavaSedaProfileGenerator_unit.bat %BDD_PROPERTIES% %CONFIG_LOCATION% %LOGBACK_DIR% %URI% %AGREEMENT% %ARCHIVE_FOLDER% %DATA_FILE% %OUT_SUMMARY% %OUT_SUMMARY_ERROR%
pause
