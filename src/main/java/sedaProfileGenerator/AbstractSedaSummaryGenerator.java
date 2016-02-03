package sedaProfileGenerator;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exception.TechnicalException;

/**
 * SedaSummaryGenerator est une classe abstraite qui offre les méthodes nécessaires pour générer un bordereau de
 * transfert SEDA
 * 
 * Trois types de données sont nécessaires pour générer un bordereau - un profil (ce peut être un fichier RNG, un schéma
 * XSD, un schematron, ...) - un ensemble de descriptions de documents avec quelques métadonnées et un identifiant
 * d'unité documentaire du profil - un ensemble d'informations : le producteur, les dates les plus anciennes et
 * récentes, le cycle de vie des documents
 * 
 * Cette classe utilise un tracesWriter pour tracer les étapes du processus de transformation
 * 
 * Toutes les erreurs détectées sont mémorisées dans une liste
 * 
 * Plusieurs implémentations peuvent être écrites, par exemple RNG, XSD, SCHEMATRON
 * 
 * Comment utiliser cette classe : Créer un objet appeler setArchiveDocuments avec un objet dérivé de ArchiveDocuments
 * éventuellement appeler setTracesWriter call generateElements() call close() getErrorList()
 * 
 * **/
public abstract class AbstractSedaSummaryGenerator {
	/*
	 * Toutes ces informations sont nécessaires pour produire un bordereau Des exceptions seront lancées si ces
	 * variables sont fausses
	 */
	protected boolean profileLoaded = false;
	protected boolean archiveDocumentsLoaded = false;
	protected boolean informationsLoaded = false;

	protected AbstractArchiveDocuments archiveDocuments;

	private static final Logger TRACESWRITER = LoggerFactory.getLogger(AbstractSedaSummaryGenerator.class);
	protected boolean traceActions = true;

	protected ArrayList<String> errorsList;
	protected boolean errorsListCompleted = false;

	public AbstractSedaSummaryGenerator() {
		errorsList = new ArrayList<String>();
		archiveDocuments = null;
	}

	/**
	 * un profil est toujours contenu dans un fichier
	 **/
	abstract protected void prepareProfileWithFile(String profileFile) throws TechnicalException;

	/**
	 * La liste des documents à archiver est définie dans un fichier qui contient le nom des fichiers et leurs
	 * métadonnées Les documents sont dans le répertoire documentsFilePath
	 **/
	abstract protected void prepareArchiveDocumentsWithFile(String documentsFilePath, String archiveDocumentsFile)
			throws TechnicalException;

	/**
	 * Ferme la connexion à la base de données
	 **/
	abstract protected void closeDatabase();

	/**
	 * Lance la génération du bordereau à partir de la lecture du profil en deux passes. (methode de remplacement)
	 * {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractSedaSummaryGenerator#generateSummaryFile(java.lang.String)
	 */
	abstract protected void generateSummaryFile(String summaryFile) throws TechnicalException;

}
