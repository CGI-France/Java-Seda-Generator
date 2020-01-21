package unit;

import commonClasses.GeneratorConfig;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sedaProfileGenerator.Checker;
import sedaProfileGenerator.SedaSummaryRngGenerator;

import commonClasses.SimpleConfig;

import dao.ConfigFileDao;
import dao.SedaSummaryRngGeneratorDao;
import exception.TechnicalException;

public class SedaGeneratorUnit {
	private static final Logger TRACESWRITER = LoggerFactory.getLogger(SedaGeneratorUnit.class);
	private static final Integer DEFAULT_NB_ARG = 8;
	private static final Integer REWRITE_NB_ARG = 6; // fichier métier, répertoire documents, fichier bordereau
	private static final Integer TASK_NB_ARG = 3;
	public static final String ERR_EXTENSION = ".err";

	private static final String ERROR_WAITING_ARGUMENTS_1 = "On attend 3, 6 ou 9 arguments : propertiesLocation, configLocation + (task OU metier, repDocuments, bordereau OU uri, agreement, archiveFolder, dataFile, outSummary, outSummaryError), ";
	private static final String ERROR_WAITING_ARGUMENTS_4 = " ont été passés.";
	private static final String ERROR_GENERATING_1 = "Erreurs lors de la tentative de génération de ";
	private static final String ERROR_GENERATING_2 = ", impossible de les écrire dans le fichier ";
	private static final String ERROR_NEITHER_CONFIG_NOR_BDD = "Il n'y a ni fichier de configuration ayant une section accord-versement ni de base de données de paramétrés.";

	private static final String TRACE_BEGIN_PRODUIRE_BEGIN_URL = "Début de produireBordereauVersement(url=";
	private static final String TRACE_BEGIN_PRODUIRE_USER = "; user=";
	private static final String TRACE_BEGIN_PRODUIRE_URI = "; uri=";
	private static final String TRACE_BEGIN_PRODUIRE_AGREEMENT = "; agreement=";
	private static final String TRACE_BEGIN_PRODUIRE_FOLDER_ARCHIVE_PATH = "; folderArchivePath=";
	private static final String TRACE_BEGIN_PRODUIRE_DATA_PATH = "; dataPath=";
	private static final String TRACE_BEGIN_PRODUIRE_SUMMARY_PATH = "; summaryPath=";
	private static final String TRACE_BEGIN_PRODUIRE_SUMMARY_PATH_ERROR = "; summaryPathError=";
	private static final String TRACE_BEGIN_CONF_PRODUIRE_BEGIN_URL = "Début de produireBordereauVersement avec ce fichier de configuration : ";

	private SedaGeneratorProperties properties;
	private String configFilePath;
	private boolean hasAccordVersementConfig = false;
	private SimpleConfig simpleConfig;
	private String url;
	private String user;
	private String passwd;
	private String uri;
	private String agreement;
	private String folderArchivePath;
	private String dataPath;
	private String summaryPath;
	private String summaryPathError;

	public SedaGeneratorUnit(String sedaGeneratorPropertiesLocation, String configFilePath, String uriLocal,
			String agreementLocal, String folderArchivePathLocal, String dataPathLocal, String summaryPathLocal,
			String summaryPathErrorLocal) throws TechnicalException {

		if (StringUtils.isNotEmpty(configFilePath)) {
			Checker.checkFile(configFilePath);
			this.simpleConfig = new SimpleConfig();
			this.simpleConfig.loadFile(configFilePath);
			if (this.simpleConfig.hasAccordVersementConfig()) {
				this.hasAccordVersementConfig = true;
			}
		}

		// S'il n'y a pas de fichier de configuration avec accord de versement, on utilise la base de données
		if (!this.hasAccordVersementConfig) {
			Checker.checkFile(sedaGeneratorPropertiesLocation);
			this.properties = new SedaGeneratorProperties(sedaGeneratorPropertiesLocation);
			this.url = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_URL.toString());
			this.user = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_USER.toString());
			this.passwd = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_PASSWD.toString());
			// On vérifie les propriétés venant du fichier de propriété
			Checker.checkDbUrl(this.url);
			Checker.checkString(this.user);
			Checker.checkString(this.passwd);
		}

		Checker.checkString(uriLocal); // Rien n'empêche que ce soit juste une chaîne de caractères, il suffit qu'elle
										// mappe la valeur en BDD.
		Checker.checkString(agreementLocal);
		Checker.checkFolder(folderArchivePathLocal);
		Checker.checkFile(dataPathLocal);
		Checker.checkParentFolder(summaryPathLocal); // On vérifie que le dossier devant contenir le bordereau de sortie
														// existe
		Checker.checkParentFolder(summaryPathErrorLocal); // On vérifie que le dossier devant contenir le fichier
															// d'erreur existe

		this.configFilePath = configFilePath;
		this.uri = uriLocal;
		this.agreement = agreementLocal;
		this.folderArchivePath = folderArchivePathLocal;
		this.dataPath = dataPathLocal;
		this.summaryPath = summaryPathLocal;
		this.summaryPathError = summaryPathErrorLocal;

	}

	public SedaGeneratorUnit(String sedaGeneratorPropertiesLocation, String configFilePath, String task) 
            throws TechnicalException {

		if (StringUtils.isNotEmpty(configFilePath)) {
			Checker.checkFile(configFilePath);
			this.simpleConfig = new SimpleConfig();
			this.simpleConfig.loadFile(configFilePath);
			if (this.simpleConfig.hasAccordVersementConfig()) {
				this.hasAccordVersementConfig = true;
			}
		}

		// S'il n'y a pas de fichier de configuration avec accord de versement, on utilise la base de données
		if (!this.hasAccordVersementConfig) {
			Checker.checkFile(sedaGeneratorPropertiesLocation);
			this.properties = new SedaGeneratorProperties(sedaGeneratorPropertiesLocation);
			this.url = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_URL.toString());
			this.user = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_USER.toString());
			this.passwd = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_PASSWD.toString());
			// On vérifie les propriétés venant du fichier de propriété
			Checker.checkDbUrl(this.url);
			Checker.checkString(this.user);
			Checker.checkString(this.passwd);
		}

        GeneratorConfig generator = this.simpleConfig.getGeneratorConfig(task);
        if (generator != null) {
            Checker.checkString(generator.getBaseURI()); // Rien n'empêche que ce soit juste une chaîne de caractères, il suffit qu'elle
                                            // mappe la valeur en BDD.
            Checker.checkString(generator.getAccordVersement());
            Checker.checkFolder(generator.getRepDocuments());
            Checker.checkFile(generator.getDataFile());
            Checker.checkParentFolder(generator.getBordereauFile()); // On vérifie que le dossier devant contenir le bordereau de sortie
                                                            // existe
            Checker.checkParentFolder(generator.getTraceFile()); // On vérifie que le dossier devant contenir le fichier
															// d'erreur existe

            this.configFilePath = configFilePath;
            this.uri = generator.getBaseURI();
            this.agreement = generator.getAccordVersement();
            this.folderArchivePath = generator.getRepDocuments();
            this.dataPath = generator.getDataFile();
            this.summaryPath = generator.getBordereauFile();
            this.summaryPathError = generator.getTraceFile();
        }
	}

	public SedaGeneratorUnit(String sedaGeneratorPropertiesLocation, String configFilePath, String task,
			String metierFile, String repertoireDocuments, String bordereauFile) 
            throws TechnicalException {

		if (StringUtils.isNotEmpty(configFilePath)) {
			Checker.checkFile(configFilePath);
			this.simpleConfig = new SimpleConfig();
			this.simpleConfig.loadFile(configFilePath);
			if (this.simpleConfig.hasAccordVersementConfig()) {
				this.hasAccordVersementConfig = true;
			}
		}

		// S'il n'y a pas de fichier de configuration avec accord de versement, on utilise la base de données
		if (!this.hasAccordVersementConfig) {
			Checker.checkFile(sedaGeneratorPropertiesLocation);
			this.properties = new SedaGeneratorProperties(sedaGeneratorPropertiesLocation);
			this.url = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_URL.toString());
			this.user = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_USER.toString());
			this.passwd = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_PASSWD.toString());
			// On vérifie les propriétés venant du fichier de propriété
			Checker.checkDbUrl(this.url);
			Checker.checkString(this.user);
			Checker.checkString(this.passwd);
		}

        GeneratorConfig generator = this.simpleConfig.getGeneratorConfig(task);
        if (generator != null) {
            Checker.checkString(generator.getBaseURI()); // Rien n'empêche que ce soit juste une chaîne de caractères, il suffit qu'elle
                                            // mappe la valeur en BDD.
            Checker.checkString(generator.getAccordVersement());
            Checker.checkFolder(generator.getRepDocuments());
            Checker.checkFile(generator.getDataFile());
            Checker.checkParentFolder(generator.getBordereauFile()); // On vérifie que le dossier devant contenir le bordereau de sortie
                                                            // existe
            Checker.checkParentFolder(generator.getTraceFile()); // On vérifie que le dossier devant contenir le fichier
															// d'erreur existe

            this.configFilePath = configFilePath;
            this.uri = generator.getBaseURI();
            this.agreement = generator.getAccordVersement();
            this.folderArchivePath = repertoireDocuments;
            this.dataPath = metierFile;
            this.summaryPath = bordereauFile;
            this.summaryPathError = generator.getTraceFile();
        }
	}

	public static void main(String[] args) {
		String sedaGeneratorPropertiesLocation;
		String configFilePath;
		String task;
		String uriLocal;
		String agreementLocal;
		String folderArchivePathLocal;
		String dataPathLocal;
		String summaryPathLocal;
		String summaryPathErrorLocal;
		SedaGeneratorUnit sedaGeneratorUnit;
		if (args.length == DEFAULT_NB_ARG) {
			try {
				sedaGeneratorPropertiesLocation = args[0];
				configFilePath = args[1];
				uriLocal = args[2];
				agreementLocal = args[3];
				folderArchivePathLocal = args[4];
				dataPathLocal = args[5];
				summaryPathLocal = args[6];
				summaryPathErrorLocal = args[7];
				sedaGeneratorUnit = new SedaGeneratorUnit(sedaGeneratorPropertiesLocation, configFilePath, uriLocal,
						agreementLocal, folderArchivePathLocal, dataPathLocal, summaryPathLocal, summaryPathErrorLocal);
				sedaGeneratorUnit.produireBordereauVersement();
			} catch (TechnicalException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			} catch (IllegalArgumentException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			}
		} else if (args.length == TASK_NB_ARG) {
			try {
				sedaGeneratorPropertiesLocation = args[0];
				configFilePath = args[1];
				task = args[2];
				sedaGeneratorUnit = new SedaGeneratorUnit(sedaGeneratorPropertiesLocation, configFilePath, task) ;
				sedaGeneratorUnit.produireBordereauVersement();
			} catch (TechnicalException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			} catch (IllegalArgumentException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			}
		} else if (args.length == REWRITE_NB_ARG) {
			try {
				sedaGeneratorPropertiesLocation = args[0];
				configFilePath = args[1];
				task = args[2];
				String metierFile = args[3];
				String rep_documents = args[4];
				String bordereauFile = args[5];
				sedaGeneratorUnit = new SedaGeneratorUnit(sedaGeneratorPropertiesLocation, configFilePath, task,
						metierFile, rep_documents, bordereauFile) ;
				sedaGeneratorUnit.produireBordereauVersement();
			} catch (TechnicalException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			} catch (IllegalArgumentException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			}
		} else {
			TRACESWRITER.error(ERROR_WAITING_ARGUMENTS_1 +
                    args.length + ERROR_WAITING_ARGUMENTS_4);
			System.exit(1);
		}
		System.exit(0);
	}

	/**
	 * Produit un bordereau de versement à partir d'un dossier d'archives, d'un fichier de données métiers et du profil
	 * dont l'emplacment est récupéré de la base de données.
	 * 
	 */
	public void produireBordereauVersement() throws TechnicalException {
		SedaSummaryRngGenerator ssg = new SedaSummaryRngGenerator();

		// S'il existe un fichier de configuration avec une section accord-versement, on l'utilise
		if (this.hasAccordVersementConfig) {

			TRACESWRITER.trace(TRACE_BEGIN_CONF_PRODUIRE_BEGIN_URL + this.configFilePath);
			ssg.setSedaSummaryRngGeneratorDao(new ConfigFileDao(this.simpleConfig, this.dataPath, ssg));

		} else {

			Checker.checkString(this.url);
			Checker.checkString(this.user);
			Checker.checkString(this.passwd);

			TRACESWRITER.trace(TRACE_BEGIN_PRODUIRE_BEGIN_URL + url + TRACE_BEGIN_PRODUIRE_USER + this.user
					+ TRACE_BEGIN_PRODUIRE_URI + this.uri + TRACE_BEGIN_PRODUIRE_AGREEMENT + agreement
					+ TRACE_BEGIN_PRODUIRE_FOLDER_ARCHIVE_PATH + this.folderArchivePath
					+ TRACE_BEGIN_PRODUIRE_DATA_PATH + this.dataPath + TRACE_BEGIN_PRODUIRE_SUMMARY_PATH
					+ this.summaryPath + TRACE_BEGIN_PRODUIRE_SUMMARY_PATH_ERROR + this.summaryPathError);

			ssg.setSedaSummaryRngGeneratorDao(new SedaSummaryRngGeneratorDao(url, user, passwd));
		}

		if (ssg.getSedaSummaryRngGeneratorDao() == null) {
			throw new TechnicalException(ERROR_NEITHER_CONFIG_NOR_BDD);
		} else {
			try {
				ssg.processData(uri, agreement, folderArchivePath, dataPath, summaryPath);
			} catch (TechnicalException e) {
				ssg.addErrorsList(e.getLocalizedMessage());
			}
		}

		// Affichage des erreurs dans la sortie d'erreur standard
		ArrayList<String> errors = ssg.getErrorsList();	    
		for (String str : errors) {
	    	System.err.println(str);
		}
		try {
			boolean existsErrors = ssg.writeErrorsIfExist(summaryPathError);

			if (existsErrors) {
				System.exit(2);
			}
		} catch (TechnicalException e) {
			throw new TechnicalException(ERROR_GENERATING_1 + summaryPath + ERROR_GENERATING_2 + summaryPathError, e);
		}

	}
}