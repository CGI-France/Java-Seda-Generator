package unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sedaProfileGenerator.Checker;
import sedaProfileGenerator.SedaSummaryRngGenerator;
import dao.SedaSummaryRngGeneratorDao;
import exception.TechnicalException;

public class SedaGeneratorUnit {
	private static final Logger TRACESWRITER = LoggerFactory.getLogger(SedaGeneratorUnit.class);
	private static final Integer DEFAULT_NB_ARG = 7;
	public static final String ERR_EXTENSION = ".err";

	private static final String ERROR_WAITING_ARGUMENTS_1 = "On attend ";
	private static final String ERROR_WAITING_ARGUMENTS_2 = " arguments (propertiesLocation, uri, agreement, archiveFolder, dataFile, outSummary, outSummaryError), ";
	private static final String ERROR_WAITING_ARGUMENTS_3 = " ont étés passés.";
	private static final String ERROR_GENERATING_1 = "Erreurs lors de la tentative de génération de ";
	private static final String ERROR_GENERATING_2 = ", impossible de les écrire dans le fichier ";

	private static final String TRACE_BEGIN_PRODUIRE_BEGIN_URL = "Début de produireBordereauVersement(url=";
	private static final String TRACE_BEGIN_PRODUIRE_USER = "; user=";
	private static final String TRACE_BEGIN_PRODUIRE_URI = "; uri=";
	private static final String TRACE_BEGIN_PRODUIRE_AGREEMENT = "; agreement=";
	private static final String TRACE_BEGIN_PRODUIRE_FOLDER_ARCHIVE_PATH = "; folderArchivePath=";
	private static final String TRACE_BEGIN_PRODUIRE_DATA_PATH = "; dataPath=";
	private static final String TRACE_BEGIN_PRODUIRE_SUMMARY_PATH = "; summaryPath=";
	private static final String TRACE_BEGIN_PRODUIRE_SUMMARY_PATH_ERROR = "; summaryPathError=";

	private SedaGeneratorProperties properties;
	private String url;
	private String user;
	private String passwd;
	private String uri;
	private String agreement;
	private String folderArchivePath;
	private String dataPath;
	private String summaryPath;
	private String summaryPathError;

	public SedaGeneratorUnit(String sedaGeneratorPropertiesLocation, String uriLocal, String agreementLocal,
			String folderArchivePathLocal, String dataPathLocal, String summaryPathLocal, String summaryPathErrorLocal)
			throws TechnicalException {
		Checker.checkFile(sedaGeneratorPropertiesLocation);
		Checker.checkString(uriLocal); // Rien n'empêche que ce soit juste une chaîne de caractères, il suffit qu'elle
										// mappe la valeur en BDD.
		Checker.checkString(agreementLocal);
		Checker.checkFolder(folderArchivePathLocal);
		Checker.checkFile(dataPathLocal);
		Checker.checkParentFolder(summaryPathLocal); // On vérifie que le dossier devant contenir le bordereau de sortie
														// existe
		Checker.checkParentFolder(summaryPathErrorLocal); // On vérifie que le dossier devant contenir le fichier
															// d'erreur existe

		this.properties = new SedaGeneratorProperties(sedaGeneratorPropertiesLocation);
		this.url = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_URL.toString());
		this.user = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_USER.toString());
		this.passwd = this.properties.getProperty(SedaGeneratorPropertiesEnum.DATABASE_PASSWD.toString());
		this.uri = uriLocal;
		this.agreement = agreementLocal;
		this.folderArchivePath = folderArchivePathLocal;
		this.dataPath = dataPathLocal;
		this.summaryPath = summaryPathLocal;
		this.summaryPathError = summaryPathErrorLocal;

		// On vérifie les propriétés venant du fichier de propriété
		Checker.checkDbUrl(this.url);
		Checker.checkString(this.user);
		Checker.checkString(this.passwd);

	}

	public static void main(String[] args) {
		String sedaGeneratorPropertiesLocation;
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
				uriLocal = args[1];
				agreementLocal = args[2];
				folderArchivePathLocal = args[3];
				dataPathLocal = args[4];
				summaryPathLocal = args[5];
				summaryPathErrorLocal = args[6];
				sedaGeneratorUnit = new SedaGeneratorUnit(sedaGeneratorPropertiesLocation, uriLocal, agreementLocal,
						folderArchivePathLocal, dataPathLocal, summaryPathLocal, summaryPathErrorLocal);
				sedaGeneratorUnit.produireBordereauVersement();
			} catch (TechnicalException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			} catch (IllegalArgumentException e) {
				TRACESWRITER.error(e.getLocalizedMessage());
				System.exit(1);
			}
		} else {
			TRACESWRITER.error(ERROR_WAITING_ARGUMENTS_1 + DEFAULT_NB_ARG + ERROR_WAITING_ARGUMENTS_2 + args.length
					+ ERROR_WAITING_ARGUMENTS_3);
			System.exit(1);
		}
	}

	/**
	 * Produit un bordereau de versement à partir d'un dossier d'archives, d'un fichier de données métiers et du profil
	 * dont l'emplacment est récupéré de la base de données.
	 * 
	 */
	public void produireBordereauVersement() throws TechnicalException {
		Checker.checkString(this.url);
		Checker.checkString(this.user);
		Checker.checkString(this.passwd);

		TRACESWRITER.trace(TRACE_BEGIN_PRODUIRE_BEGIN_URL + url + TRACE_BEGIN_PRODUIRE_USER + this.user
				+ TRACE_BEGIN_PRODUIRE_URI + this.uri + TRACE_BEGIN_PRODUIRE_AGREEMENT + agreement
				+ TRACE_BEGIN_PRODUIRE_FOLDER_ARCHIVE_PATH + this.folderArchivePath + TRACE_BEGIN_PRODUIRE_DATA_PATH
				+ this.dataPath + TRACE_BEGIN_PRODUIRE_SUMMARY_PATH + this.summaryPath
				+ TRACE_BEGIN_PRODUIRE_SUMMARY_PATH_ERROR + this.summaryPathError);

		SedaSummaryRngGenerator ssg = new SedaSummaryRngGenerator();
		ssg.setSedaSummaryRngGeneratorDao(new SedaSummaryRngGeneratorDao(url, user, passwd));
		try {
			ssg.processData(uri, agreement, folderArchivePath, dataPath, summaryPath);
		} catch (TechnicalException e) {
			ssg.addErrorsList(e.getLocalizedMessage());
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