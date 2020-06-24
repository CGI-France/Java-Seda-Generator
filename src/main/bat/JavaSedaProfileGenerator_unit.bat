@ECHO OFF
REM Ce script est un exemple de lancement de la génération d'un bordereau.
REM PARAMETRES :
REM - propertiesLocation : chemin vers un fichier renseignant "DATABASE_URL=" "DATABASE_USER=" et "DATABASE_PASSWD="
REM - uri (Colonne SAE_Serveur de la table SAE): première partie de la clé (uri, agreement) permettant de récupérer le chemin vers le profil en base de données.
REM - agreement (Colonne SAE_AccordVersement de la table SAE) : deuxième partie de la clé (uri, agreement) permettant de récupérer le chemin vers le profil en base de données.
REM - archiveFolder : chemin vers le dossier racine de l'archive (non compressé)
REM - dataFile : chemin vers le fichier de données métiers à utiliser pour la génération du bordereau
REM - outSummary : chemin vers le bordereau de sortie.
REM - outSummaryError : chemin vers le fichier d'erreur à créer, plutôt que le bordereau, si la génération de ce bordereau est en erreur.
REM CODES D'ERREUR :
REM - 0 : Succès.
REM - 1 : Erreur grave, non tracées dans OUT_SUMMARY
REM - 2 : Erreurs, tracées dans OUT_SUMMARY

REM ARGUMENTS
SET PROPERTIES_LOCATION=%1
SET CONFIG_LOCATION=%2
SET LOGBACK_DIR=%3
SET URI=%4
SET AGREEMENT=%5
SET ARCHIVE_FOLDER=%6
SET DATA_FILE=%7
SET OUT_SUMMARY=%8
SET OUT_SUMMARY_ERROR=%9

REM LIB
SET LOGBACK_LIB=lib/logback-classic-1.0.7.jar
SET LOGBACK_CORE_LIB=lib/logback-core-1.0.7.jar
SET POSTGRESQL_LIB=lib/postgresql-9.4-1201-jdbc41.jar
SET SAXON_LIB=lib/saxon-8.7.jar
SET SERIALIZER_LIB=lib/serializer-2.7.1.jar
SET SLF4J_LIB=lib/slf4j-api-1.7.12.jar
SET XALAN_LIB=lib/xalan-2.7.1.jar
SET XMLAPIS_LIB=lib/xml-apis-1.3.04.jar
SET COMMONS_LANG_LIB=lib/commons-lang-2.3.jar
SET JAVASEDAPROFILEGENERATOR=lib/JavaSedaProfileGenerator-1.1.3.jar

SET JAVA_BIN=java

SET CLASSPATH=%LOGBACK_LIB%;%LOGBACK_CORE_LIB%;%POSTGRESQL_LIB%;%SAXON_LIB%;%SERIALIZER_LIB%;%SLF4J_LIB%;%XALAN_LIB%;%XMLAPIS_LIB%;%COMMONS_LANG_LIB%;%JAVASEDAPROFILEGENERATOR%;%LOGBACK_DIR%

%JAVA_BIN% -classpath %CLASSPATH% unit.SedaGeneratorUnit %PROPERTIES_LOCATION% %CONFIG_LOCATION% %URI% %AGREEMENT% %ARCHIVE_FOLDER% %DATA_FILE% %OUT_SUMMARY% %OUT_SUMMARY_ERROR%
