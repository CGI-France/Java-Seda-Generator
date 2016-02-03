package sedaProfileGenerator;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exception.TechnicalException;

public abstract class AbstractArchiveDocuments {
	protected boolean traceActions = true;

	protected ArrayList<String> errorsList = new ArrayList<String>();

	private static final Logger TRACESWRITER = LoggerFactory.getLogger(AbstractArchiveDocuments.class);

	public AbstractArchiveDocuments() {

	}

	/**
	 * Retourne le nombre de documents référençant le type docListType
	 */
	abstract public boolean isThereDocumentsReferringToType(String docListType);

	/**
	 * Prépare une liste de documents qui sont identifiés comme docListType retourne le nombre de documents
	 */
	abstract public int prepareListForType(String docListType, boolean withDocumentIdentification); // il
																									// faudrait
																									// mettre
																									// withDocumentIdentification
																									// à
																									// false
																									// si
																									// non
																									// donné

	/**
	 * Prépare une liste contenant tous les documents
	 */
	abstract public int prepareCompleteList();

	/**
	 * Se positionne sur le document suivant dans la liste préparée par prepareListForType ou prepareCompleteList
	 */
	abstract public boolean nextDocument();

	/**
	 * Donne le nom de fichier du document courant (positionné par nextDocument ou prepareCompleteList ou
	 * prepareListForType
	 */
	abstract public String getFileName();

	/**
	 * Donne le nom du document courant (positionné par nextDocument ou prepareCompleteList ou prepareListForType
	 */
	abstract public String getName();

	/**
	 * Donne le date du document courant (positionné par nextDocument ou prepareCompleteList ou prepareListForType
	 */
	abstract public String getDocumentDate();

	/**
	 * Donne l'algorithme de l'empreinte pour l'élément courant (positionné par nextDocument ou prepareCompleteList ou
	 * prepareListForType)
	 */
	abstract public String getHashAlgorithm();

	/**
	 * Donne l'empreinte pour l'élément courant (positionné par nextDocument ou prepareCompleteList ou
	 * prepareListForType)
	 */
	abstract public String getHash();

	/**
	 * Donne la date la plus récente de la liste préparée par prepareCompleteList ou prepareListForType
	 */
	abstract public String getLatestDate() throws TechnicalException;

	/**
	 * Donne la date la plus ancienne de la liste préparée par prepareCompleteList ou prepareListForType
	 */
	abstract public String getOldestDate() throws TechnicalException;

	/**
	 * Donne la valeur de la clé
	 */
	abstract public String getKeyValue(String key);

	/**
	 * Donne le nombre de clés pour un tag de document (#cle_tag#num) Si documentTag est vide ou null, seule la clé est
	 * cherchée (#cle#num)
	 */
	abstract public int getNbkeys(String key, String documentTag, String typeTag);

	/**
	 * Donne la valeur de la clé pour un tag de document (#cle_tag#num) Si documentTag est vide ou null, seule la clé
	 * est cherchée (#cle#num)
	 */
	abstract public String getNextKeyValue(String key, String documentTag, String typeTag);

	/**
	 * Retourne la liste des erreurs rencontrées
	 */
	public ArrayList<String> getErrorsList() {
		return errorsList;
	}

	/**
	 * Permet d'ajouter une erreur
	 */
	protected void addActionError(String action) {
		errorsList.add("Unable to perform action '" + action + "'. No matching element found in ArchiveDocuments"); // Devient
																													// bloquant
																													// par
																													// l'appartenance
																													// à
																													// la
																													// liste
																													// d'erreur,
																													// mais
																													// ne
																													// dois
																													// pas
																													// bloquer
																													// l'analyse
																													// d'éventuelle
																													// autres
																													// erreurs.
		TRACESWRITER.error("Unable to perform action '" + action + "'. No matching element found in ArchiveDocuments");
	}
}
