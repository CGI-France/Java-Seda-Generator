@ECHO OFF
REM Ce script est un exemple de lancement de la génération d'un bordereau.
REM PARAMETRES :
REM - task : tâche à exécuter dans le fichier de configuration
REM CODES D'ERREUR :
REM - 0 : Succès.
REM - 1 : Erreur grave, non tracées dans OUT_SUMMARY
REM - 2 : Erreurs, tracées dans OUT_SUMMARY

REM ARGUMENTS
SET TASK=%1

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
SET JAVASEDAPROFILEGENERATOR=lib/JavaSedaProfileGenerator-1.0.2.jar

SET JAVA_BIN=java

SET CLASSPATH=%LOGBACK_LIB%;%LOGBACK_CORE_LIB%;%POSTGRESQL_LIB%;%SAXON_LIB%;%SERIALIZER_LIB%;%SLF4J_LIB%;%XALAN_LIB%;%XMLAPIS_LIB%;%COMMONS_LANG_LIB%;%JAVASEDAPROFILEGENERATOR%;%LOGBACK_DIR%

%JAVA_BIN% -classpath %CLASSPATH% unit.SedaGeneratorUnit ./sedaGeneratorUnit.properties ./job.config %TASK%
