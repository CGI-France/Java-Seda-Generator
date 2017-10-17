package sedaProfileGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exception.TechnicalException;

/**
 * Cette classe charge les informations concernant les fichiers à archiver à partir d'un fichier CSV Ce fichier doit
 * respecter les règles suivantes : Chaque ligne représente un document à archiver Chaque ligne commence par un
 * séparateur. Cette technique permet de changer de séparateur à chaque ligne Les deux premiers champs sont obligatoires
 * Champ 0: séparateur de la ligne Champ 1: nom de fichier relatif Champ 2: identification du document (DOCLIST) Champ
 * 3: nom du document Champ 4: date de production ou réception du document Champ 5: algorithme de l'empreinte fournie à
 * insérer dans le bordereau Champ 6: empreinte fournie
 * 
 * Ce ficheir peut aussi contenir des informations de type clé / valeur Les identifiants des clés commencent par # et
 * sont suivis par un séparateur et une valeur #TransferName, value
 * **/
public class CsvArchiveDocuments extends AbstractArchiveDocuments {
	private static final Logger TRACESWRITER = LoggerFactory.getLogger(CsvArchiveDocuments.class);

	public static final String BEGINNING_TAG_CAR = "[";
	public static final String END_TAG_CAR = "]";

	private ArrayList<String[]> documentsList = new ArrayList<String[]>();
	private ArrayList<String[]> keyList = new ArrayList<String[]>();

	private ArrayList<String[]> partialDocumentsList = new ArrayList<String[]>();
	private Enumeration<String[]> partialDocumentsListEnumerator;
	private String[] currentPartialDocument = null;
	private String partialDocumentsListCurrentDocType = null;
	private String currentArchiveObjectIdentifier = null;
	private String lastError;

	private String oldestDate = null;
	private String latestDate = null;

	private String currentKey2search;
	private int currentKey2searchCounter;

	private static final String BEGINNING_CAR = "#";
	public static final String BEGINNING_TYPE_CAR = "{";
	private static final String BEGINNING_OCCUR_CAR = "[";
	public static final String END_TYPE_CAR = "}";
	private static final String END_OCCUR_CAR = "]";
	private static final String ECHAP_CAR = "\\";
	private static final String UTF8_BOM = "\uFEFF";
	private static final String ENCODAGE = "UTF8";
	private static final String DOCUMENT_TAG_BEGINNING = "{";
	private static final String DEFAULT_OLDEST_DATE = "9999-12-31";
	private static final String DEFAULT_LATEST_DATE = "1970-01-01";
	private static final String ERROR_NO_DATA = "Pas de données chargées";
	private static final String ERROR_NO_DOCUMENTS_FOR_TYPE_BEGIN = "Pas de documents pour '";
	private static final String ERROR_NO_DOCUMENTS_FOR_TYPE_END = "'";
	private static final String ERROR_NO_DOCUMENTS = "Liste de documents vide";
	private static final String PATTERN_ANY_CHAR = ".*";
	private static final String EMPTY = "";
	private static final String SEPARATOR = "/";

	private static final String DATE_PATTERN_1 = "yyyy-MM-dd";
	private static final String DATE_PATTERN_2 = "yyyy-MM-dd' 'HH:mm:ss";
	private static final String DATE_PATTERN_3 = "dd/MM/yyyy' 'HH:mm:ss";
	private static final String DATE_PATTERN_4 = "dd/MM/yyyy";

	private static final String ERROR_MALFORMED_LINE = "Le traitement s'est interrompu car une ligne des données métier est mal construite.";
	private static final String ERROR_NO_FILE_1 = "Le fichier ";
	private static final String ERROR_NO_FILE_2 = " n'a pas ete trouvé.";
	private static final String ERROR_READING = "Erreur lors de la lecture du fichier ";
	private static final String ERROR_REFER = " réfère ";
	private static final String ERROR_NO_REFER = " ne réfère pas ";
	private static final String ERROR_REFER_END = "à des documents";
	private static final String ERROR_NOT_FOUND = "Error not found";
	private static final String ERROR_DATAERR_BEGIN = "#DATAERR:";
    private static final String ERROR_INCORRECT_NUMBER_OF_SEPARATOR = "#DATAERR: Nombre de séparateurs incorrect en ligne '";
	private static final String ERROR_DATAERR_FILENAME = "#DATAERR: Le nom de fichier du document n'a pas été trouvé dans : '";
	private static final String ERROR_DATAERR_NAME = "#DATAERR: Le nom du document n'a pas été trouvé dans : '";
	private static final String ERROR_DATAERR_DATE = "#DATAERR: La date du document n'a pas été trouvée dans : '";
	private static final String ERROR_DATAERR_TAG_1 = "#DATAERR: Le tag : '";
	private static final String ERROR_DATAERR_TAG_2 = "' n'a pas été trouvé dans les données métier";
	private static final String ERROR_QUOTE = "'";
	private static final String ERROR_COMPUTE_DATES_DOC_IDENTIFIED = "Impossible de calculer les dates extrêmes car la date de ce document n'a pas été trouvée : ";
	private static final String ERROR_COMPUTE_DATES = "Impossible de calculer les dates extrêmes car la date d'un document n'a pas été trouvée.";

	private static final String TRACE_BEGIN_LOAD_FILE_1 = "Début de CsvArchiveDocuments.loadFile('";
	private static final String TRACE_BEGIN_LOAD_FILE_2 = "')";
	private static final String TRACE_BEGIN_IS_THERE_DOCUMENTS_REFERRING_TO_TYPE_1 = "Début de CsvArchiveDocuments.IsThereDocumentsReferringToType('";
	private static final String TRACE_BEGIN_IS_THERE_DOCUMENTS_REFERRING_TO_TYPE_2 = "')";
	private static final String TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_1 = "Début de CsvArchiveDocuments.prepareListForType('";
	private static final String TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_2 = "')";
	private static final String TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_RESULT_1 = "CsvArchiveDocuments.prepareListForType found '";
	private static final String TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_RESULT_2 = "' documents";
	private static final String TRACE_BEGIN_PREPARE_COMPLETE_LIST = "Début de CsvArchiveDocuments.prepareCompleteList()";

	private static final int NB_TAG_LINE_VALUE_FIELDS = 2;
	private static final int NB_MIN_DOCUMENT_LINE_FIELDS = 2;
	private static final int NB_MAX_DOCUMENT_LINE_FIELDS = 7;	// Il y a deux champs en plus lorsque l'empreinte est renseignée
																// Il y a trois champs en plus lorsque la taille est renseignée
	private static final int FILENAME_LOCATION = 1;
	private static final int TYPE_LOCATION = 2;
	private static final int NAME_LOCATION = 3;
	private static final int DATE_LOCATION = 4;
	private static final int ALGO_LOCATION = 5;
	private static final int HASH_LOCATION = 6;
	private static final int SIZE_LOCATION = 7;

	public CsvArchiveDocuments() {

		lastError = ERROR_NO_DATA;
		currentKey2search = EMPTY;
		currentKey2searchCounter = 1;

	}

        /*
         * Indique si la ligne est une ligne de données ou une ligne de commentaires
         * Les lignes de commentaires commencent par une espace ou une tabulation
         *
         * @param line la ligne de données à vérifier
         * @return true si la ligne est une ligne de données
         * @return false si la ligne est une ligne de commentaire (commence par espace ou tabulation
         * */
        private boolean isThisLineALineOfData(String line) {
            return line.length() > 0 && line.charAt(0) != '#';
        }

	/**
	 * Charge un fichier de données métiers
	 *
	 * @param csvFile chemin vers le fichier de données métiers
	 * @throws TechnicalException
	 */
	public void loadFile(String csvFile) throws TechnicalException {

		TRACESWRITER.trace(TRACE_BEGIN_LOAD_FILE_1 + csvFile + TRACE_BEGIN_LOAD_FILE_2);
		String rgxSeperator;
		String line;
		String[] elements;
		int elementsLength;
		int currentLine = 0;
		boolean isLineMalformed;

		Checker.checkFile(csvFile);

		try {
			File f = new File(csvFile);
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, ENCODAGE);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				currentLine++;
				isLineMalformed = false;
                line = removeUTF8BOM(line);
                if ( isThisLineALineOfData(line) ) {
                    rgxSeperator = EMPTY + line.charAt(0);
                    elements = line.split(rgxSeperator);
                    elementsLength = elements.length;
                    if (elementsLength > 2) {
                        if (StringUtils.isNotEmpty(elements[1]) && elements[1].startsWith(BEGINNING_CAR)) {
                            if (elementsLength != (NB_TAG_LINE_VALUE_FIELDS + 1)) {
                                isLineMalformed = true;
                            } else {
                                keyList.add(elements);
                            }
                        } else {
                            if (elementsLength < (NB_MIN_DOCUMENT_LINE_FIELDS + 1)
                                    || elementsLength > (NB_MAX_DOCUMENT_LINE_FIELDS + 1)) {
                                isLineMalformed = true;
                            } else {
                                documentsList.add(elements);
                            }
                        }
                    } else {
                        isLineMalformed = true;
                    }
                    if (isLineMalformed) {
                        logAndAddErrorsList(ERROR_MALFORMED_LINE + Arrays.toString(elements));
                    }
				}
				lastError = EMPTY;
			}
			br.close();
		} catch (FileNotFoundException e) {
			throw new TechnicalException(ERROR_NO_FILE_1 + csvFile + ERROR_NO_FILE_2, e);
		} catch (IOException e) {
			throw new TechnicalException(ERROR_READING + csvFile, e);
		}

	}

	/**
	 * Récupération du contexte sans le tag du document DOCUMENTEZ_MOI
	 *
	 * @param part
	 * @return
	 */
	private String getTagWithoutDocumentIdentification(String part) {

		if (part.contains(DOCUMENT_TAG_BEGINNING)) { // marqueur des
														// Identification de
														// Document
			int pos = part.indexOf(DOCUMENT_TAG_BEGINNING);
			part = part.substring(0, pos);
		}
		return part;

	}

	/**
	 * Retourne le nombre de documents référençant le type docListType (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#isThereDocumentsReferringToType(java.lang.String)
	 */
	@Override
	public boolean isThereDocumentsReferringToType(String docListType) {

		TRACESWRITER.trace(TRACE_BEGIN_IS_THERE_DOCUMENTS_REFERRING_TO_TYPE_1 + docListType
				+ TRACE_BEGIN_IS_THERE_DOCUMENTS_REFERRING_TO_TYPE_2);
		boolean atLeastOne = false;
		for (int i = 0; i < documentsList.size(); i++) {
			String[] elements = documentsList.get(i);
			if (elements[TYPE_LOCATION].contains(SEPARATOR)) {
				String split[] = elements[TYPE_LOCATION].split(SEPARATOR);
				for (int j = 0; j < split.length; j++) {
					String part = split[j];
					if (getTagWithoutDocumentIdentification(part).equals(docListType)) {
						atLeastOne = true;
						break;
					}
				}
				if (atLeastOne)
					break;
			} else {
				if (getTagWithoutDocumentIdentification(elements[TYPE_LOCATION]).equals(docListType)) {
					atLeastOne = true;
					break;
				}
			}
		}
		String str_refer = atLeastOne ? ERROR_REFER : ERROR_NO_REFER;
		TRACESWRITER.debug(docListType + str_refer + ERROR_REFER_END);
		return atLeastOne;

	}

	/**
	 * Prépare la liste des documents pour un type donné (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#prepareListForType(java.lang.String, boolean)
	 */
	@Override
	public int prepareListForType(String docListType, boolean withDocumentIdentification) {

		// Si la liste a déjà été préparée pour ce type de document, on ne fait rien
		if (partialDocumentsListCurrentDocType != null && docListType.equals(partialDocumentsListCurrentDocType))
			return partialDocumentsList.size();
		
		TRACESWRITER.trace(TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_1 + docListType + TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_2);
		int counter = 0;
		partialDocumentsList.clear();
		partialDocumentsListCurrentDocType = docListType;
		for (int i = 0; i < documentsList.size(); i++) {
			String[] elements = documentsList.get(i);
			if ((withDocumentIdentification == true && elements[TYPE_LOCATION].equals(docListType))
					|| (getTagWithoutDocumentIdentification(elements[TYPE_LOCATION]).equals(docListType))) {
				partialDocumentsList.add(elements);
				counter++;
			}
		}
		TRACESWRITER.trace(TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_RESULT_1 + partialDocumentsList.size()
				+ TRACE_BEGIN_PREPARE_LIST_FOR_TYPE_RESULT_2);
		if (partialDocumentsList.size() != 0) {
			partialDocumentsListEnumerator = Collections.enumeration(partialDocumentsList);
			lastError = EMPTY;
		} else {
			lastError = ERROR_NO_DOCUMENTS_FOR_TYPE_BEGIN + docListType + ERROR_NO_DOCUMENTS_FOR_TYPE_END;
			counter = 0;
		}
		return counter;

	}

	/**
	 * Prépare une liste contenant tous les documents (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#prepareCompleteList()
	 */
	@Override
	public int prepareCompleteList() {

		int counter = 0;
		TRACESWRITER.trace(TRACE_BEGIN_PREPARE_COMPLETE_LIST);
		partialDocumentsList.clear();
		partialDocumentsList.addAll(documentsList);
		if (partialDocumentsList.size() != 0) {
			partialDocumentsListEnumerator = Collections.enumeration(partialDocumentsList);
			lastError = "";
			counter = partialDocumentsList.size();
		} else {
			lastError = ERROR_NO_DOCUMENTS;
			counter = 0;
		}
		return counter;

	}

	/**
	 * Se positionne sur le document suivant dans la liste préparée par prepareListForType ou prepareCompleteList
	 * (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#nextDocument()
	 */
	@Override
	public boolean nextDocument() {

		boolean next = false;
		if (StringUtils.isEmpty(lastError)) {
			if (partialDocumentsListEnumerator.hasMoreElements()) {
				currentPartialDocument = (String[]) partialDocumentsListEnumerator.nextElement();
				next = true;
			}
		}
		return next;

	}

	/**
	 * Donne le chemin du document courant (positionné par nextDocument ou prepareCompleteList ou prepareListForType
	 * (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getFileName()
	 */
	public String getFileName() {

		String fileNameLocal = null;
		StringBuilder errorMessage;
		if (StringUtils.isEmpty(lastError)) {
			String[] tabCurrent = currentPartialDocument;
			if (tabCurrent == null) {
				errorMessage = new StringBuilder().append(ERROR_DATAERR_FILENAME).append(tabCurrent.toString())
						.append(ERROR_QUOTE);
				addActionError(errorMessage.toString());
				fileNameLocal = ERROR_NOT_FOUND;
			} else {
				fileNameLocal = tabCurrent[FILENAME_LOCATION];
			}
		}
		if (fileNameLocal == null) {
			fileNameLocal = lastError;
		}
		return fileNameLocal;

	}

	/**
	 * Donne le nom du document courant (positionné par nextDocument ou prepareCompleteList ou prepareListForType
	 * (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getName()
	 */
	@Override
	public String getName() {

		String nameLocal = null;
		StringBuilder errorMessage;

		if (StringUtils.isEmpty(lastError)) {
			String[] tabCurrent = currentPartialDocument;
			if (tabCurrent == null) {
				errorMessage = new StringBuilder().append(ERROR_DATAERR_NAME).append(tabCurrent.toString())
						.append(ERROR_QUOTE);
				addActionError(errorMessage.toString());
				nameLocal = ERROR_NOT_FOUND;
			} else {
				nameLocal = tabCurrent[NAME_LOCATION];
			}
		}
		if (nameLocal == null) {
			nameLocal = lastError;
		}
		return nameLocal;

	}

	/**
	 * Donne le date du document courant (positionné par nextDocument ou prepareCompleteList ou prepareListForType
	 * (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getDocumentDate()
	 */
	@Override
	public String getDocumentDate() {

		String dateLocal = null;
		StringBuilder errorMessage;

		if (StringUtils.isEmpty(lastError)) {
			String[] tabCurrent = currentPartialDocument;
			if (tabCurrent == null) {
				errorMessage = new StringBuilder().append(ERROR_DATAERR_DATE).append(tabCurrent.toString())
						.append(ERROR_QUOTE);
				addActionError(errorMessage.toString());
				dateLocal = ERROR_NOT_FOUND;
			} else {
				dateLocal = tabCurrent.length > 4 ? tabCurrent[4] : "";
			}
		}
		if (dateLocal == null) {
			dateLocal = lastError;
		}
		return dateLocal;

	}

	/**
	 * Permet de récupérer l'algorithme utilisé pour l'empreinte des données métier
	 *
	 * @return l'algorithme de calcul d'empreinte si il est présent dans les données métier, null sinon
	 */
	@Override
	public String getDocumentHashAlgorithm() {

		String returnHashAlgorithm;

		String[] tabCurrent = currentPartialDocument;
		returnHashAlgorithm = null;

		if (tabCurrent != null && tabCurrent.length >= (HASH_LOCATION + 1)) {
			if (StringUtils.isNotEmpty(tabCurrent[ALGO_LOCATION]) && StringUtils.isNotEmpty(tabCurrent[HASH_LOCATION])) {
				returnHashAlgorithm = tabCurrent[ALGO_LOCATION];
			}
		}

		return returnHashAlgorithm;

	}

	/**
	 * Permet de récupérer l'empreinte des données métier
	 *
	 * @return l'empreinte si elle est présente dans les données métier, null sinon
	 */
	@Override
	public String getDocumentHash() {

		String returnHash;

		String[] tabCurrent = currentPartialDocument;
		returnHash = null;

		if (tabCurrent != null && tabCurrent.length >= (HASH_LOCATION + 1)) {
			if (StringUtils.isNotEmpty(tabCurrent[ALGO_LOCATION]) && StringUtils.isNotEmpty(tabCurrent[HASH_LOCATION])) {
				returnHash = tabCurrent[HASH_LOCATION];
			}
		}

		return returnHash;

	}

	/**
	 * Permet de récupérer la taille du document fournie par les données métier
	 *
	 * @return la taille si elle est présente dans les données métier, null sinon
	 */
	@Override
	public String getDocumentSize() {

		String returnSize = null;

		if (currentPartialDocument != null && currentPartialDocument.length >= (SIZE_LOCATION + 1)) {
			returnSize = currentPartialDocument[SIZE_LOCATION];
		}

		return returnSize;

	}
	
	private boolean elementMatchesDocumentIdentifier(String elementType, String archiveObjectIdentifier) {
		// On enlève progressivement de elementType les parties d'identifiant jusqu'à trouver une correspondance
		// On ne traite pas les documents (par ex :  NODE1//NODE2[#3]//NODE4{DOC}
		// Ex : NODE1//NODE2[#3]//NODE4
		// se décompose en :
		// NODE1//NODE2[#3]//NODE4
		// NODE1//NODE2[#3]
		// NODE1
		// "root" correspond à tous les documents
		if (archiveObjectIdentifier.equals("root"))
			return true;
		int indexSeparator = 0;
		while (indexSeparator != -1) {
			if (elementType.equals(archiveObjectIdentifier))
				return true;
			indexSeparator = elementType.lastIndexOf("//");
			if (indexSeparator != -1)
				elementType = elementType.substring(0, indexSeparator);
		}

		return false;
	}

	/**
	 * Calcule les dates extrêmes pour une unité documentaire
	 *
	 */
	@Override
	public void computeDates(String archiveObjectIdentifier) throws TechnicalException {

		TRACESWRITER.trace("computeDates (" + archiveObjectIdentifier + ") through " + documentsList.size() + " documents");

		oldestDate = DEFAULT_OLDEST_DATE;
		latestDate = DEFAULT_LATEST_DATE;
		String[] elements;
		String dateString = "";
		SimpleDateFormat pattern;
		SimpleDateFormat sdfDefault = new SimpleDateFormat(DATE_PATTERN_1);
		Date date;

		List<String> knownPatterns = new ArrayList<String>();
		knownPatterns.add(DATE_PATTERN_2);
		knownPatterns.add(DATE_PATTERN_3);
		knownPatterns.add(DATE_PATTERN_1);
		knownPatterns.add(DATE_PATTERN_4);
		int nbMatchingDocs = 0;
		for (int i = 0; i < documentsList.size(); i++) {
			elements = documentsList.get(i);
			if (elementMatchesDocumentIdentifier(elements[TYPE_LOCATION], archiveObjectIdentifier)) {
				++nbMatchingDocs;
				if (elements.length > DATE_LOCATION) {
					dateString = elements[DATE_LOCATION];
					for (String patternString : knownPatterns) {
						try {
							Date oldest = sdfDefault.parse(oldestDate);
							Date latest = sdfDefault.parse(latestDate);
							pattern = new SimpleDateFormat(patternString);
							date = pattern.parse(dateString);
							if (date.before(oldest))
								oldestDate = sdfDefault.format(date);
							if (date.after(latest))
								latestDate = sdfDefault.format(date);
							break;
						} catch (ParseException pe) {
						} // On essaie plusieurs patterns
					}
				} else {
					if (elements.length > FILENAME_LOCATION && !StringUtils.isEmpty(elements[FILENAME_LOCATION])) {
						throw new TechnicalException(ERROR_COMPUTE_DATES_DOC_IDENTIFIED + elements[FILENAME_LOCATION]);
					} else {
						throw new TechnicalException(ERROR_COMPUTE_DATES);
					}
				}
			}
		}
		currentArchiveObjectIdentifier = archiveObjectIdentifier;
		TRACESWRITER.trace("OldestDate = '" + oldestDate + "' latestDate = '" + latestDate + "' nbMatchingDocs '" + nbMatchingDocs + "'");

	}

	/**
	 * Donne la date la plus récente calculée par computeDates (methode de
	 * remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getLatestDate()
	 */
	@Override
	public String getLatestDate(String archiveObjectIdentifier) throws TechnicalException {

		if (currentArchiveObjectIdentifier == null || (! currentArchiveObjectIdentifier.equals(archiveObjectIdentifier)))
			computeDates(archiveObjectIdentifier);
		return latestDate;

	}

	/**
	 * Donne la date la plus ancienne calculée par computeDates (methode de
	 * remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getOldestDate()
	 */
	@Override
	public String getOldestDate(String archiveObjectIdentifier) throws TechnicalException {

		if (currentArchiveObjectIdentifier == null || (! currentArchiveObjectIdentifier.equals(archiveObjectIdentifier)))
			computeDates(archiveObjectIdentifier);
		return oldestDate;

	}

	/**
	 * Donne la valeur de la clé (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getKeyValue(java.lang.String)
	 */
	@Override
	public String getKeyValue(String key) {

		StringBuilder errorMessage;

		key = BEGINNING_CAR + key;
		errorMessage = new StringBuilder().append(ERROR_DATAERR_TAG_1).append(key).append(ERROR_DATAERR_TAG_2);
		String[] elements;
		String value = errorMessage.toString();
		if (keyList == null) {
			addActionError(value);
		} else {
			for (int i = 0; i < keyList.size(); i++) {
				elements = keyList.get(i);
				if (elements[1].compareTo(key) == 0) {
					if (elements.length > 2) {
						value = elements[TYPE_LOCATION];
					}
				}
			}
		}
		if (value.startsWith(ERROR_DATAERR_BEGIN)) {
			logAndAddErrorsList(value);// TODO Le non
			// renseignement d'une valeur de clé devient bloquant
		}
		return value;
	}

	/**
	 * Donne le nombre de clé pour un tag de document (#cle_tag#num) Si documentTag est vide ou null, seule la clé est
	 * cherchée (#cle#num) (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getNbkeys(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public int getNbkeys(String key, String documentTag, String typeTag) {

		int nbKeys = 0;
		String[] elements;

		StringBuilder keyToSearch = new StringBuilder();
		StringBuilder keyToSearchPattern = new StringBuilder();
		String keyToSearchPatternString;

		boolean existsDocumentTag;
		boolean existsTypeTag;

		existsDocumentTag = documentTag != null && StringUtils.isNotEmpty(documentTag);
		existsTypeTag = typeTag != null && StringUtils.isNotEmpty(typeTag);

		key = BEGINNING_CAR + key;
		keyToSearch.append(key);

		// Quand il y a un documentTag ou un typeTag, alors on cherche la clé suivie de crochets
		if (existsDocumentTag || existsTypeTag) {
			keyToSearch.append(BEGINNING_TAG_CAR);
			if (existsDocumentTag) {
				keyToSearch.append(documentTag);
			}
			if (existsTypeTag) {
				keyToSearch.append(BEGINNING_TYPE_CAR).append(typeTag).append(END_TYPE_CAR);
			}
			keyToSearchPattern.append(keyToSearch).append(PATTERN_ANY_CHAR).append(END_TAG_CAR);
		}
		keyToSearchPatternString = keyToSearchPattern.toString()
				.replace(BEGINNING_TAG_CAR, ECHAP_CAR + BEGINNING_TAG_CAR)
				.replace(END_TAG_CAR, ECHAP_CAR + END_TAG_CAR)
				.replace(BEGINNING_TYPE_CAR, ECHAP_CAR + BEGINNING_TYPE_CAR)
				.replace(END_TYPE_CAR, ECHAP_CAR + END_TYPE_CAR);

		Pattern r = Pattern.compile(keyToSearchPatternString);
		if (keyList != null) {
			for (int i = 0; i < keyList.size(); i++) {
				elements = keyList.get(i);
				Matcher m = r.matcher(elements[1]);
				if (m.matches()) {
					nbKeys++;
				}
			}
		}
		if (keyToSearch.toString().equals(currentKey2search)) {
			nbKeys -= currentKey2searchCounter;
			if (nbKeys < 0) {
				nbKeys = 0;
			}
		}
		return nbKeys;

	}

	/**
	 * Donne la valeur de la clé pour un tag de document (#cle_tag#num) Si documentTag est vide ou null, seule la clé
	 * est cherchée (#cle#num) (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractArchiveDocuments#getNextKeyValue(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public String getNextKeyValue(String key, String documentTag, String typeTag) {
		StringBuilder errorMessage;
		StringBuilder keyToSearch = new StringBuilder();
		String[] elements;

		boolean existsDocumentTag;
		boolean existsTypeTag;

		existsDocumentTag = documentTag != null && StringUtils.isNotEmpty(documentTag);
		existsTypeTag = typeTag != null && StringUtils.isNotEmpty(typeTag);

		key = BEGINNING_CAR + key;
		keyToSearch.append(key);

		if (existsDocumentTag || existsTypeTag) {
			keyToSearch.append(BEGINNING_TAG_CAR);
			if (existsDocumentTag) {
				keyToSearch.append(documentTag);
			}
			if (existsTypeTag) {
				keyToSearch.append(BEGINNING_TYPE_CAR).append(typeTag).append(END_TYPE_CAR);
			}
			if (!currentKey2search.equals(keyToSearch.toString())) {
				currentKey2search = keyToSearch.toString();
				currentKey2searchCounter = 1;
			} else {
				currentKey2searchCounter++;
			}
			keyToSearch.append(BEGINNING_OCCUR_CAR).append(BEGINNING_CAR)
					.append(String.valueOf(currentKey2searchCounter)).append(END_OCCUR_CAR).append(END_TAG_CAR);
		}

		key = keyToSearch.toString();
		errorMessage = new StringBuilder().append(ERROR_DATAERR_TAG_1).append(key).append(ERROR_DATAERR_TAG_2);

		String value = errorMessage.toString();
		if (keyList == null) {
			addActionError(value);
		} else {
			for (int i = 0; i < keyList.size(); i++) {
				elements = keyList.get(i);
				if (elements[1].compareTo(key) == 0) {
					value = elements[TYPE_LOCATION];
				}
			}
		}
		if (value.startsWith(ERROR_DATAERR_BEGIN))
			logAndAddErrorsList(value);// TODO Le non
		// renseignement d'une valeur de clé devient bloquant
		return value;
	}

	public ArrayList<String[]> getDocumentsList() {

		return documentsList;

	}

	public ArrayList<String[]> getKeyList() {

		return keyList;

	}

	private static String removeUTF8BOM(String s) {

		if (s.startsWith(UTF8_BOM)) {
			s = s.substring(1);
		}
		return s;

	}

	/**
	 * 
	 * Cette méthode permet : - de loguer dans slf4j une "error", sachant que ce log contient tous les messages de
	 * toutes les générations de bordereaux faites par le module - d'ajouter l'erreur à errorsList, pour ainsi créer un
	 * fichier d'erreurs associé à chacun des bordereaux.
	 */
	public void logAndAddErrorsList(String error) {
		TRACESWRITER.error(error);
		errorsList.add(error);
	}
}
