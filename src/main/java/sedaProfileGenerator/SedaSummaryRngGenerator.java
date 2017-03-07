package sedaProfileGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import metier.Sae;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dao.interfaces.SedaSummaryRngGeneratorDaoInterface;
import exception.TechnicalException;

/**
 * SedaSummaryRngGenerator est une implémentation de SedaSummaryGenerator qui s'appuie sur un profil au format RNG
 * SedaSummaryRngGenerator.java
 */
public class SedaSummaryRngGenerator extends AbstractSedaSummaryGenerator {

	private static final Logger TRACESWRITER = LoggerFactory.getLogger(SedaSummaryRngGenerator.class);

	// Balises RelaxNG
	private static final String RNG_REF = "rng:ref";
	private static final String RNG_ELEMENT = "rng:element";
	private static final String RNG_ONEORMORE = "rng:oneOrMore";
	private static final String RNG_ZEROORMORE = "rng:zeroOrMore";
	private static final String RNG_OPTIONAL = "rng:optional";
	private static final String RNG_VALUE = "rng:value";
	private static final String RNG_ATTRIBUTE = "rng:attribute";
	private static final String RNG_DATA = "rng:data";

	// Balises SEDA
	private static final String ROOT = "root";
	private static final String NOEUD_DOCUMENT = "Document";
	private static final String NOEUD_CONTAINS = "Contains";
	private static final String NOEUD_ARCHIVE_OBJECT = "ArchiveObject";
	private static final String NOEUD_ARCHIVE = "Archive";
	private static final String NOEUD_KEYWORDCONTENT = "KeywordContent";
	private static final String NOEUD_KEYWORD = "Keyword";
	private static final String NOEUD_CONTENTDESCRIPTIVE = "ContentDescriptive";
	private static final String NOEUD_FILEPLANPOSITION = "FilePlanPosition";
	private static final String NOEUD_ARCHIVETRANSFER = "ArchiveTransfer";
	private static final String NOEUD_UNITIDENTIFIER = "UnitIdentifier";
	private static final String NOEUD_INTEGRITY = "Integrity";
    private static final String NOEUD_ARCHIVALAGENCYDOCUMENTIDENTIFIER = "ArchivalAgencyDocumentIdentifier";
    private static final String NOEUD_IDENTIFICATION = "Identification";

	// Attributs SEDA
    private static final String SEDA_VERSION_02 = "fr:gouv:ae:archive:draft:standard_echange_v0.2";
    private static final String SEDA_VERSION_10 = "fr:gouv:culture:archivesdefrance:seda:v1.0";
	private static final String ATTR_NAME_FILENAME = "filename";
	private static final String ATTR_NAME_ALGORITHME = "algorithme";
	private static final String ATTR_NAME_SCHEME_ID = "schemeID";
	private static final String ATTR_NAME_NAME = "name";
	private static final String ATTR_NAME_MIMECODE = "mimeCode";
	private static final String XMLNS = "xmlns";
	private static final String NODE_VALUE_ANY_ELEMENT = "anyElement";
	private static final String COMMENT = "#comment";
	private static final String ITEM_NAME = "name";
	private static final String ITEM_NS = "ns";
	private static final String UNITCODE_NAME = "unitCode";
	private static final String UNITCODE_E36 = "E36";
	private static final String UNITCODE_E35 = "E35";
	private static final String UNITCODE_E34 = "E34";
	private static final String UNITCODE_4L = "4L";
	private static final String UNITCODE_2P = "2P";
	private static final String UNITCODE_AD = "AD";
	private static final String TAG_RECEIPT = "Receipt";
	private static final String TAG_TYPE = "Type";
	private static final String TAG_ISSUE = "Issue";
	private static final String TAG_DURATION = "Duration";
	private static final String TAG_CREATION = "Creation";
	private static final String TAG_OLDESTDATE = "OldestDate";
	private static final String TAG_STARTDATE = "StartDate";
	private static final String TAG_LATESTDATE = "LatestDate";
	private static final String TAG_DATE = "Date";
	private static final String TAG_INTEGRITY = "Integrity";

	// Contextes SEDA
	private static final String CONTEXT_END_DOCATT = "Document/Attachment";
	private static final String CONTEXT_DOCUNIT_SIZE = "Contains/ContentDescription/Size";
	private static final String CONTEXT_END_DOCSIZE = "Document/Size";
	private static final String CONTEXT_END_INTEGRITY = "Integrity";

	// Données métiers
	private static final String KEY_TRANSFERNAME = "TransferName";
	private static final String KEY_COMMENT = "Comment";
	private static final String KEY_CUSTODIALHISTORY = "CustodialHistory";
	private static final String KEY_CONTENTDESCRIPTION = "ContentDescription";
	private static final String KEY_DESCRIPTION = "Description";
	private static final String KEY_FILEPLANPOSITION = "FilePlanPosition";
	private static final String KEY_ORIGINATINGAGENCY = "OriginatingAgency";
	private static final String KEY_KEYWORDCONTENT = "KeywordContent";
	private static final String KEY_CONTAINSNAME = "ContainsName";
	private static final String KEY_CONTAINS = "Contains";
	private static final String KEY_NAME = "Name";
	private static final String KEY_TAG_SEPARATOR = ".";

	// Ecriture du bordereau
	private static final String TRANSFO_INDENT_NUMBER_ATTR = "indent-number";
	private static final Integer TRANSFO_INDENT_NUMBER_VALUE = 4;
	private static final String TRANSFO_METHOD = "xml";
	private static final String TRANSFO_OMIT_XML_DECLARATION = "no";
	private static final String TRANSFO_ENCODING = "utf-8";
	private static final String WRITING_ENCODING = "UTF-8";
	private static final String TRANSFO_INDENT = "yes";
	private static final String PROCESSING_EXTENSION = ".processing";

	// Arbre DOM
	private static final String DT_ATTRIBUT = "attribut";
	private static final String DT_CDATA = "CDATA";
	private static final String DT_COMMENT = "comment";
	private static final String DT_DOCUMENT_FRAGMENT = "document fragment";
	private static final String DT_DOCUMENT = "document";
	private static final String DT_DOCUMENT_TYPE = "document type";
	private static final String DT_NODE = "node";
	private static final String DT_ENTITY = "entity";
	private static final String DT_ENTITY_REFERENCE = "entity reference";
	private static final String DT_NOTATION = "notation";
	private static final String DT_PROCESSING_INSTRUCTION = "processing instruction";
	private static final String DT_TEXT = "text";
	private static final String DT_NONE = "none";

	private static final String DEFAULT_ALGORITHM = HashAlgorithm.SHA256.getHashAlgorithmUrl();
	private static final String FORMAT_DATE_BORDEREAU = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";

	private static final String TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE = "-----------------------------------------";
	private static final String TRACE_BEGIN_GENERATE_SUMMARY_FILE_1 = "Début de SedaSummaryRngGenerator.generateSummaryFile('";
	private static final String TRACE_BEGIN_GENERATE_SUMMARY_FILE_2 = "')";
	private static final String TRACE_BEGIN_GENERATE_SUMMARY_FILE_PASS_1 = "Début de l'évaluation du nombre de documents";
	private static final String TRACE_BEGIN_GENERATE_SUMMARY_FILE_PASS_2 = "Début de génération du bordereau";
	private static final String TRACE_END_GENERATE_SUMMARY_FILE_PASS_1 = "Fin de l'évaluation du nombre de documents";
	private static final String TRACE_END_GENERATE_SUMMARY_FILE_PASS_2 = "Fin de génération du bordereau ";
	private static final String TRACE_NO_ERROR_TO_WRITE = "Pas d'erreurs à écrire dans le fichier ";
	private static final String TRACE_BEGIN_ERRORS_LIST = "Debut de la liste des erreurs";
	private static final String TRACE_END_ERRORS_LIST = "Fin de la liste des erreurs";

	private static final String ERROR_NODE_NOT_FOUND_1 = "Le noeud '";
	private static final String ERROR_NODE_NOT_FOUND_2 = "' n'a pas été trouvé dans le profil '";
	private static final String ERROR_NODE_NOT_FOUND_3 = "'";
	private static final String ERRORS_NUMBER_1 = "Nombre d'erreurs : ";
	private static final String ERRORS_NUMBER_2 = " pendant la génération du bordereau : ";
	private static final String ERROR_PARSE_DATE = "La date n'a pa pu être récupérée";
	private static final String ERRORS_IN_FILE = "ERREURS dans le fichier : ";
	private static final String ERRORS_UNABLE_TO_WRITE_IN_FILE = "Impossible d'écrire les erreurs dans le fichier : ";
	private static final String ERROR_TRANSFORMATION_FAILED = "La transformation a échoué : ";
	private static final String ERROR_TEMP_DOESNT_EXIST_1 = "Le fichier temporaire ";
	private static final String ERROR_TEMP_DOESNT_EXIST_2 = " n'existe plus.";
	private static final String ERROR_RENAME_1 = "Impossible de renommer ";
	private static final String ERROR_RENAME_2 = " en ";
	private static final String ERROR_DELETE = "Impossible de supprimer ";

	// Récupération des informations liées à un couple (URI/ACCORD) en base de données.
	private Sae sae;
	private Connection conn = null;

	// Attributs utilisés pour la génération d'un bordereau
	private Document docIn;
	private Document docOut;
	private Node grammarNode;
    private String SEDA_version;

	// La génération se fait en deux passes, durant la première on calcule le nombre de documents par unité documentaire
	// les erreurs sont inhibées durant cette phase
	private int currentPass;
	private ContainsNode rootContainsNode;
	private ContainsNode currentContainsNode;

	// Le premier nœud Contains doit être généré indépendamment du fait qu'il y ait ou pas des documents. Ce premier
	// nœud ne contient pas de balise ArchivalAgencyObjectIdentifier et doit être traité de façon exceptionnelle.
	private boolean firstContainsNode = true;
	private String currentDocumentTypeId;
	private long objectIdentifier = 0;
	private String SAE_FilePath;
	private Element elementCourantW;
	private Node currentNodeW;

	private SedaSummaryRngGeneratorDaoInterface sedaSummaryRngGeneratorDao = null;

	private boolean multipleSearch = false; // Quand ce booléen est à true, on cherche les données métiers suivies de
											// l'occurrence (getNextKeyValue et non getKeyValue)

	public SedaSummaryRngGenerator() {
		// rootContainsNode est marqué avec l'ID "root"
		rootContainsNode = new ContainsNode(ROOT, null, true);
		currentContainsNode = rootContainsNode;
		currentPass = 1;
	}

	/**
	 * Méthode permettant de lancer la génération d'un bordereau.
	 *
	 * @param baseURI
	 * @param accordVersement
	 * @param folder
	 * @param data
	 * @param summary
	 * @throws TechnicalException
	 */
	public void processData(String baseURI, String accordVersement, String folder, String data, String summary)
			throws TechnicalException {

		Checker.checkString(baseURI);
		Checker.checkString(accordVersement);
		Checker.checkFolder(folder);
		Checker.checkFile(data);
		Checker.checkParentFolder(summary);

		try {

			sae = getSedaSummaryRngGeneratorDao().getSae(baseURI, accordVersement);

		} catch (TechnicalException e) {

			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

		try {
			prepareProfileWithFile(sae.getProfileFile());
		} catch (TechnicalException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

		try {
			prepareArchiveDocumentsWithFile(folder, data);
		} catch (TechnicalException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

		generateSummaryFile(summary);
	}

	/**
	 * La liste des documents à archiver est définie dans un fichier qui contient le nom des fichiers et leurs
	 * métadonnées (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractSedaSummaryGenerator#prepareProfileWithFile(java.lang.String)
	 */
	@Override
	protected void prepareProfileWithFile(String profileFile) throws TechnicalException {

		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			docIn = builder.parse(new File(profileFile)); // Chargement du
															// profil en objet
															// DOM
															// Element rootElement = docIn.getDocumentElement();
			// printDomTree(rootElement, "", ""); // affichage de l'arbre du profil dans les traces.
			profileLoaded = true;
		} catch (ParserConfigurationException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		} catch (SAXException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

	}

	/**
	 * La liste des documents à archiver est définie dans un fichier qui contient les noms des fichiers et leurs
	 * métadonnées Les documents sont dans le répertoire documentsFilePath (methode de remplacement) {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractSedaSummaryGenerator#prepareArchiveDocumentsWithFile(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected void prepareArchiveDocumentsWithFile(String documentsFilePath, String archiveDocumentsFile)
			throws TechnicalException {

		SAE_FilePath = documentsFilePath;
		archiveDocumentsLoaded = false;
		CsvArchiveDocuments ad = new CsvArchiveDocuments();
		ad.loadFile(archiveDocumentsFile);
		// La variable membre héritée contient la valeur de l'objet créé
		archiveDocuments = ad;
		archiveDocumentsLoaded = true;

	}

	/**
	 * Ferme la connexion à la base de données
	 */
	protected void closeDatabase() {

		try {
			conn.close();
		} catch (SQLException e) {
			logAndAddErrorsList(e.getLocalizedMessage());
		}

	}

	/**
	 * Lance la génération du bordereau à partir de la lecture du profil en deux passes. (methode de remplacement)
	 * {@inheritDoc}
	 * 
	 * @see sedaProfileGenerator.AbstractSedaSummaryGenerator#generateSummaryFile(java.lang.String)
	 */
	@Override
	protected void generateSummaryFile(String summaryFile) throws TechnicalException {

		Node startNode = null;
		NodeList nodes;
		if (currentPass == 1) {
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_1 + summaryFile + TRACE_BEGIN_GENERATE_SUMMARY_FILE_2);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_PASS_1);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
		}
		if (currentPass == 2) {
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_1 + summaryFile + TRACE_BEGIN_GENERATE_SUMMARY_FILE_2);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_PASS_2);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
			// Create the XmlDocument.
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			try {
				docBuilder = docFactory.newDocumentBuilder();
				docOut = docBuilder.newDocument();
			} catch (ParserConfigurationException e) {
				throw new TechnicalException(e.getLocalizedMessage(), e);
			}
		}

		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			nodes = (NodeList) xPath.evaluate("/grammar", docIn.getDocumentElement(), XPathConstants.NODESET);
			if (nodes.getLength() > 0) {
				grammarNode = nodes.item(0);
			}
			nodes = (NodeList) xPath.evaluate("/grammar/start/ref", docIn.getDocumentElement(), XPathConstants.NODESET);
			if (nodes.getLength() > 0) {
				startNode = nodes.item(0);
			}
			if (grammarNode != null && startNode != null) {
                    // SEDA 1.0 "fr:gouv:culture:archivesdefrance:seda:v1.0"
                    // SEDA 0.2 "fr:gouv:ae:archive:draft:standard_echange_v0.2"
                    String sTestSeda = grammarNode.getAttributes().getNamedItem(ITEM_NS).getNodeValue();
                    if (SEDA_VERSION_02.equals(sTestSeda)) {
                        SEDA_version = "0.2";
                    } else
                        if (SEDA_VERSION_10.equals(sTestSeda)) {
                            SEDA_version = "1.0";
                        } else {
                            SEDA_version = "Version du SEDA inconnue";
                            logAndAddErrorsList("Version du SEDA inconnue : '" + sTestSeda + "'");
                        }
				recurseDefine(startNode.getAttributes().getNamedItem(ITEM_NAME).getNodeValue(), "");
			} else {
				// On pourrait tracer l'erreur seulement quand currentPass = 2
				if (grammarNode == null) {
					throw new TechnicalException(ERROR_NODE_NOT_FOUND_1 + "rng:grammar" + ERROR_NODE_NOT_FOUND_2
							+ sae.getProfileFile() + ERROR_NODE_NOT_FOUND_3);
				} else {
					throw new TechnicalException(ERROR_NODE_NOT_FOUND_1 + "rng:grammar/rng:start/rng:ref"
							+ ERROR_NODE_NOT_FOUND_2 + sae.getProfileFile() + ERROR_NODE_NOT_FOUND_3);
				}
			}
		} catch (XPathExpressionException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}
		if (currentPass == 2) {
			int nbErrorsLocal = getNbErrors();
			//if (nbErrorsLocal == 0) {
				ecrireDocument(docOut, summaryFile);
				TRACESWRITER.debug(ERRORS_NUMBER_1 + nbErrorsLocal + ERRORS_NUMBER_2 + summaryFile);
			//} // Sinon le bordereau n'est pas généré.
		}

		if (currentPass == 1) {
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
			TRACESWRITER.trace(TRACE_END_GENERATE_SUMMARY_FILE_PASS_1);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
			rootContainsNode.computeNbDocuments();
			boolean bAllMandatoryContainsHaveDocuments = true;
			String sListMandatoryContainsInError = "";
			ContainsNode node = rootContainsNode;
			while (node != null) {
				if (node.getMandatory() == true && node.getNbDocuments() == 0) {
					bAllMandatoryContainsHaveDocuments = false;
					sListMandatoryContainsInError += node.getName();
					sListMandatoryContainsInError += "\t";
				}
				TRACESWRITER.debug("next node = " + node.getName() + " docs = " + node.getNbDocuments()
						+ " mandatory = " + node.getMandatory());
				node = node.next();
			}
			if (bAllMandatoryContainsHaveDocuments == false) {
				logAndAddErrorsList("Il existe des unités documentaires obligatoires sans documents : "
						+ sListMandatoryContainsInError); // On ne lance pas de
															// TechnicalException
															// pour continuer de
															// lister les
															// erreurs
			} else {
				currentPass = 2;
				rootContainsNode.trunkEmptyBranches();
				currentContainsNode = rootContainsNode;
				firstContainsNode = true; // Indique que le prochain nœud
											// Contains est le premier
				currentDocumentTypeId = "root";
				generateSummaryFile(summaryFile);
			}
		} else {
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
			TRACESWRITER.trace(TRACE_END_GENERATE_SUMMARY_FILE_PASS_2 + summaryFile);
			TRACESWRITER.trace(TRACE_BEGIN_GENERATE_SUMMARY_FILE_LINE);
		}

	}

	/**
	 * Cette méthode recherche le contenu de l'attribut schemeID de la balise ArchivalAgencyObjectIdentifier de la
	 * balise containsNode Arborescence dans laquelle on recherche le ArchivalAgencyObjectIdentifier <rng:optional>
	 * <rng:element name="Contains"> => on part d'ici <rng:ref name="Contains_N66109"/> </rng:element> </rng:optional>
	 * 
	 * <rng:define name="Contains_N66109"> <rng:optional> <rng:element name= "ArchivalAgencyObjectIdentifier"> <rng:ref
	 * name= "ArchivalAgencyObjectIdentifier_N66114"/> </rng:element> <rng:element name="DescriptionLevel"> <rng:ref
	 * name="DescriptionLevel_N66149"/> </rng:optional> </rng:element>
	 * 
	 * <rng:define name= "ArchivalAgencyObjectIdentifier_N66114"> <rng:data type="string"/> <rng:attribute
	 * name="schemeID"> <rng:value> DOCLIST_20140604 / DOCLIST_ESPCO_CODIR </rng:value> </rng:attribute>
	 */
	private String lookupForContainsIdentifier(Node containsNode) throws TechnicalException {
		String containsIdentifier = "";
		XPath xPath = null;
		NodeList firstNodeList = null;
		Node currentNode;
		Node firstStep = null;
		NodeList thirdNodeList = null;
		Node thirdStep;
		NodeList fourthNodeList = null;
		Node fourthStep;
		if (firstContainsNode == true) {
			containsIdentifier = ROOT;
			firstContainsNode = false;
		} else {
			firstNodeList = containsNode.getChildNodes();
			for (int i = 0; i < firstNodeList.getLength(); i++) {
				currentNode = firstNodeList.item(i);
				if (RNG_REF.equals(currentNode.getNodeName())) {
					firstStep = currentNode; // On récupère la référence du
												// Contains.
				}
			}
			if (firstStep != null) {
				xPath = XPathFactory.newInstance().newXPath();
				try {
					thirdNodeList = (NodeList) xPath.evaluate("/grammar/define[@name='"
							+ firstStep.getAttributes().getNamedItem(ITEM_NAME).getNodeValue()
							+ "']//element[@name='ArchivalAgencyObjectIdentifier']/ref", docIn.getDocumentElement(),
							XPathConstants.NODESET); // En référence à la
														// troisième étape.
					if (thirdNodeList.getLength() > 0) {
						thirdStep = thirdNodeList.item(0); // Un seul noeud nous
															// intéresse
						if (thirdStep != null) {
							fourthNodeList = (NodeList) xPath.evaluate("/grammar/define[@name='"
									+ thirdStep.getAttributes().getNamedItem(ITEM_NAME).getNodeValue()
									+ "']//attribute[@name='schemeID']/value", docIn.getDocumentElement(),
									XPathConstants.NODESET);
							if (fourthNodeList.getLength() > 0) {
								fourthStep = fourthNodeList.item(0);
								if (fourthStep != null) {
									containsIdentifier = getDocumentTypeId(fourthStep.getFirstChild().getTextContent(),
											""); // On récupère le tag de
													// l'unité documentaire.
								}
							}
						}
					}
				} catch (XPathExpressionException e) {
					throw new TechnicalException(e.getLocalizedMessage(), e);
				}
			} else {
				throw new TechnicalException("Le noeud  " + containsNode.getNodeName()
						+ " n'a pas de noeud rng:ref dans le RNG");
			}
		}
		return containsIdentifier;
	}

	/**
	 * Méthode utilitaire pour récupérer le DOCLIST
	 */
	private String getDocumentTypeId(String value, String context) throws TechnicalException {
		String docId = "";
		int pos = value.indexOf(" / ");
		if (pos != -1) {
			docId = value.substring(pos + 3);
		} else { // pos != -1
			if (StringUtils.isNotEmpty(context)) {
				if (currentPass == 1) {
					logAndAddErrorsList("DOCLIST identifier malformed, expected 'DOCLIST / identifier' got '" + value
							+ "' in context '" + context + "'"); // En passe 1,
																	// on ne
																	// bloque
																	// pas
																	// encore la
																	// génération
																	// du
																	// bordereau,
																	// pour
																	// lister
																	// d'autres
																	// erreurs.
				} else {
					throw new TechnicalException("DOCLIST identifier malformed, expected 'DOCLIST / identifier' got '"
							+ value + "' in context '" + context + "'"); // En
																			// passe
																			// 2,
																			// l'exception
																			// est
																			// lancée.
				}
			}
		}
		return docId;
	}

	private void doContains(Node currentNode, BGenererElement bGenererElement, BoucleTags boucleTags, int numeroTag,
			boolean bContainsIsMandatory) throws TechnicalException {
		currentDocumentTypeId = lookupForContainsIdentifier(currentNode);
		TRACESWRITER.trace("lookupForContainsIdentifier trouve currentDocumentTypeId " + currentDocumentTypeId
				+ " en passe " + currentPass);
		if (currentDocumentTypeId.endsWith("+")) {
			boucleTags.setValue(true);
			currentDocumentTypeId = formatContainsIdentifier(currentDocumentTypeId, numeroTag);
			TRACESWRITER.trace("currentDocumentTypeId à répétition : " + currentDocumentTypeId);
		}
		bGenererElement.setValue(1);
		if (boucleTags.getValue())
			if (archiveDocuments.isThereDocumentsReferringToType(currentDocumentTypeId) == false) {
				bGenererElement.setValue(0);
				boucleTags.setValue(false);
			}
		if (currentPass == 2) {
			if (bGenererElement.getValue() > 0 && currentContainsNode != null) {
				if (ROOT.equals(currentDocumentTypeId)) {
					currentContainsNode = rootContainsNode;
				} else {
					currentContainsNode = currentContainsNode.next();
				}
				if (currentContainsNode != null) { // Tant qu'on est pas rrivés
													// au bout de l'arbre
					TRACESWRITER.trace("Selecting next currentContainsNode " + currentContainsNode.getName()
							+ " that contains " + currentContainsNode.getNbDocuments() + " documents");
					int nbDocs = currentContainsNode.getNbDocuments();
					bGenererElement.setValue(nbDocs > 0 ? 1 : 0);
					TRACESWRITER.trace("DOCLIST docs " + currentDocumentTypeId + " contains " + nbDocs + " documents");
				}
			}
		} else {
			if (bGenererElement.getValue() != 0) {
				if (ROOT.equals(currentDocumentTypeId)) {
					currentContainsNode = rootContainsNode;
				} else {
					TRACESWRITER.trace("création nouveau containsNode(" + currentDocumentTypeId + ")");
					currentContainsNode = currentContainsNode.addNewNode(currentDocumentTypeId, bContainsIsMandatory);
				}
				int nbDocs = archiveDocuments.prepareListForType(currentContainsNode.getRelativeContext(), false);
				currentContainsNode.incNbDocs(nbDocs);
			}
		}
	}

	private String lookupForDocumentIdentification(Node documentNode) throws TechnicalException {
		String documentIdentification = "";
		XPath xPath = null;
		NodeList firstNodeList = null;
		Node currentNode;
		Node firstStep = null;
		NodeList thirdNodeList = null;
		Node thirdStep;
		NodeList fourthNodeList = null;
		Node fourthStep;
		firstNodeList = documentNode.getChildNodes();
		for (int i = 0; i < firstNodeList.getLength(); i++) {
			currentNode = firstNodeList.item(i);
			if (RNG_REF.equals(currentNode.getNodeName())) {
				firstStep = currentNode; // On récupère la référence du
											// Document.
			}
		}
		if (firstStep != null) {
			xPath = XPathFactory.newInstance().newXPath();
			try {
                String stDocumentIdentification = SEDA_version == "1.0" 
                        ? NOEUD_ARCHIVALAGENCYDOCUMENTIDENTIFIER : NOEUD_IDENTIFICATION;
				thirdNodeList = (NodeList) xPath
						.evaluate("/grammar/define[@name='"
								+ firstStep.getAttributes().getNamedItem("name").getNodeValue()
								+ "']//element[@name='"
                                + stDocumentIdentification
                                + "']/ref", docIn.getDocumentElement(),
								XPathConstants.NODESET); // En référence à la
															// troisième étape.
				if (thirdNodeList.getLength() > 0) {
					thirdStep = thirdNodeList.item(0); // Un seul noeud nous
														// intéresse
					if (thirdStep != null) {

						fourthNodeList = (NodeList) xPath.evaluate("/grammar/define[@name='"
								+ thirdStep.getAttributes().getNamedItem("name").getNodeValue()
								+ "']//attribute[@name='schemeID']/value", docIn.getDocumentElement(),
								XPathConstants.NODESET);
						if (fourthNodeList.getLength() > 0) {
							fourthStep = fourthNodeList.item(0);
							if (fourthStep != null) {
								documentIdentification = getDocumentTypeId(fourthStep.getFirstChild().getTextContent(),
										""); // On
												// récupère
												// le
												// tag
												// de
												// l'unité
												// documentaire.
							}
						}
					}
				}
			} catch (XPathExpressionException e) {
				throw new TechnicalException(e.getLocalizedMessage(), e);
			}
		} else {
			throw new TechnicalException("Le noeud  " + documentNode.getNodeName()
					+ " n'a pas de noeud rng:ref dans le RNG");
		}
		return documentIdentification;
	}

	/**
	 * Un define est une structure qui définit le contenu d'un élément Elle peut faire appel à des références (rng:ref)
	 * vers d'autres define et doit donc être traité récursivement
	 * 
	 * <rng:define name="Contains_N66109"> <rng:optional> <rng:element name= "ArchivalAgencyObjectIdentifier"> <rng:ref
	 * name= "ArchivalAgencyObjectIdentifier_N66114"/> </rng:element> <rng:element name="DescriptionLevel"> <rng:ref
	 * name="DescriptionLevel_N66149"/> </rng:optional> </rng:element>
	 */
	private String recurseDefine(String defineNodeName, String context) throws TechnicalException {
		TRACESWRITER.trace("recurseDefine ('" + defineNodeName + "', '" + context + "', '" + currentDocumentTypeId
				+ "')");
		String dataString = null;
		NodeList nodes = null;
		Node defineNode = null;
		NodeList defineNodeList = null;
		Node currentNode = null;
		NodeList currentNodeList = null;
		Node zomNode = null;
		Node attrNode = null;

		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			nodes = (NodeList) xPath.evaluate("/grammar/define[@name='" + defineNodeName + "']",
					docIn.getDocumentElement(), XPathConstants.NODESET);
			if (nodes.getLength() > 0) {
				defineNode = nodes.item(0);
			}
			if (defineNode != null) {
				if (defineNode.hasChildNodes()) {
					defineNodeList = defineNode.getChildNodes();
					for (int i = 0; i < defineNodeList.getLength(); i++) {
						currentNode = defineNodeList.item(i);
						if (currentNode.hasAttributes() && currentNode.getAttributes().getNamedItem(ITEM_NAME) != null) {
							TRACESWRITER.trace("recurseDefine visiting '" + currentNode.getNodeName() + "'[name='"
									+ currentNode.getAttributes().getNamedItem(ITEM_NAME).getNodeValue() + "']");
						} else {
							TRACESWRITER.trace("recurseDefine visiting '" + currentNode.getNodeName() + "'");
						}
						switch (currentNode.getNodeName()) {
						case RNG_ELEMENT:
							if (currentNode.hasAttributes()) {
								BGenererElement bGenererElement = new BGenererElement(1);
								int numeroTag = 0; // Première boucle sur
													// des
													// tags numérotés
								BoucleTags boucleTags = new BoucleTags(false);
								do {
									numeroTag++;
									String elementName = currentNode.getAttributes().getNamedItem(ITEM_NAME)
											.getNodeValue();
									if (NOEUD_CONTAINS.equals(elementName) // SEDA
																			// 0.2
											|| NOEUD_ARCHIVE_OBJECT.equals(elementName)
											|| NOEUD_ARCHIVE.equals(elementName)) { // SEDA
																					// 1.0
										doContains(currentNode, bGenererElement, boucleTags, numeroTag, true);
									}
									if (bGenererElement.getValue() > 0)
										genElement(currentNode, elementName, context);
								} while (boucleTags.getValue());
							}
							break;
						// Ici on va générer des documents. C'est là que
						// l'on
						// passe au pilotage par les documents
						// au lieu du pilotage par le profil
						case RNG_ONEORMORE:
						case RNG_ZEROORMORE:
						case RNG_OPTIONAL:
							// ici on peut boucler si le containsIdentifier
							// se
							// termine par un plus
							int numeroTag = 0; // Première boucle sur des
												// tags
												// numérotés
							BoucleTags boucleTags = new BoucleTags(false);
							do {
								numeroTag++;
								TRACESWRITER.trace("recurseDefine ??OrMore for " + currentNode.getNodeName()
										+ " in context " + context + " on defineNode " + defineNodeName);
								if (currentNode.hasChildNodes()) {
									currentNodeList = currentNode.getChildNodes();
									for (int j = 0; j < currentNodeList.getLength(); j++) {
										zomNode = currentNodeList.item(j);
										if (RNG_ELEMENT.equals(zomNode.getNodeName()) && zomNode.hasAttributes()) {
											BGenererElement bGenererElement = new BGenererElement(0);
											String elementName = zomNode.getAttributes().getNamedItem(ITEM_NAME)
													.getNodeValue();
											switch (elementName) {
											default:
												if (RNG_ONEORMORE.equals(currentNode.getNodeName()))
													bGenererElement.setValue(1);
												break;
											case NOEUD_DOCUMENT: // les
																	// documents
																	// doivent
																	// bien
																	// évidemment
																	// être
																	// générés
												bGenererElement.setValue(1);
												break;
											case NOEUD_CONTAINS: // SEDA
																	// 0.2
											case NOEUD_ARCHIVE_OBJECT: // SEDA
																		// 1.0
											case NOEUD_ARCHIVE: {
												boolean bContainsIsMandatory = false;
												if (RNG_ONEORMORE.equals(currentNode.getNodeName()))
													bContainsIsMandatory = true;
												doContains(zomNode, bGenererElement, boucleTags, numeroTag,
														bContainsIsMandatory);
											}
												break;
											case NOEUD_KEYWORDCONTENT:
											case NOEUD_KEYWORD: // SEDA
																// 1.0
											case NOEUD_CONTENTDESCRIPTIVE: // SEDA
																			// 0.2
												if (currentPass == 2) {
													String containsName = currentContainsNode.getRelativeContext();
													if (ROOT.equals(containsName)) {
														containsName = null;
													}
													Node dataNode; // Noeud rng:data
													String keywordReferenceSchemeID;

													dataNode = null;
													keywordReferenceSchemeID = null;

													dataNode = getDataKeywordNodeFromContentDescriptiveNode(zomNode); // zomNode
																														// correspond
																														// au
																														// rng:element
																														// name="ContentDescriptive"

													if (dataNode != null) {
														keywordReferenceSchemeID = getKeywordReferenceSchemeId(dataNode, context);
													}

													int nbKeywords = archiveDocuments.getNbkeys(NOEUD_KEYWORDCONTENT,
															containsName, keywordReferenceSchemeID); // TODO rajouter
																										// éventuel
																										// schemeID.
													if (nbKeywords == 0) {
														nbKeywords = (RNG_ONEORMORE.equals(currentNode.getNodeName()) ? 1
																: 0); // Si un mot clé est demandé, on va tout de même
																		// essayer d'en générer un pour avoir le message
																		// d'absence dans les données métier.
													}
													// ContentDescriptive
													// ou
													// Keyword est
													// généré
													// plusieurs fois et
													// contient un
													// KeywordContent

													// Si oneOrMore ou zeroOrMore, il faudra chercher les données
													// métiers suivies de l'occurrence, sinon sans.
													switch (currentNode.getNodeName()) {
													case RNG_ONEORMORE:
													case RNG_ZEROORMORE:
														multipleSearch = true;
														break;
													}

													bGenererElement
															.setValue(NOEUD_KEYWORDCONTENT.equals(elementName) ? 1
																	: nbKeywords);
													TRACESWRITER.trace("KeywordContent(" + containsName + ") contains "
															+ nbKeywords + " keywords");
												}
												break;
											case NOEUD_FILEPLANPOSITION:
												if (currentPass == 2) {
													String tagFilePlanPosition = ""; // Temp
													NodeList filePlanPositionChilds;
													Node refFPPNode = null;
													filePlanPositionChilds = zomNode.getChildNodes();

													for (int k = 0; k < filePlanPositionChilds.getLength(); k++) {
														if (RNG_REF
																.equals(filePlanPositionChilds.item(k).getNodeName())) {
															refFPPNode = filePlanPositionChilds.item(k);
															break;
														}
													}
													if (refFPPNode != null) {
														tagFilePlanPosition = lookupForAttribute(ATTR_NAME_SCHEME_ID,
																refFPPNode);
													}
													// int nbFilePlanPosition = archiveDocuments.getNbkeys(
													// NOEUD_FILEPLANPOSITION, tagFilePlanPosition, null);
													StringBuilder tag;
													tag = new StringBuilder();
													if (!currentDocumentTypeId.equals(ROOT)) {
														tag.append(currentContainsNode.getRelativeContext());
													}
													int nbFilePlanPosition = archiveDocuments
															.getNbkeys(NOEUD_FILEPLANPOSITION, tag.toString(),
																	tagFilePlanPosition);

													// Si oneOrMore ou zeroOrMore, il faudra chercher les données
													// métiers suivies de l'occurrence, sinon sans.
													switch (currentNode.getNodeName()) {
													case RNG_ONEORMORE:
													case RNG_ZEROORMORE:
														multipleSearch = true;
														break;
													}

													bGenererElement
															.setValue(NOEUD_FILEPLANPOSITION.equals(elementName) ? nbFilePlanPosition
																	: 1);
													TRACESWRITER
															.trace("FilePlanPosition(" + tagFilePlanPosition
																	+ ") contains " + nbFilePlanPosition
																	+ " FilePlanPositions");
												}
												break;
											}
											while (bGenererElement.getValue() != 0) {
												genElement(zomNode, elementName, context);
												bGenererElement.setValue(bGenererElement.getValue() - 1);
												// if (bGenererElement.getValue() == 0) {
												// multipleSearch = false;
												// }
											}
											if (currentPass == 2) {
												multipleSearch = false;
											}
										} // if (zomNode.Name
									} // for
								} // if currentNode.HasChildNodes
							} while (boucleTags.getValue());
							break;
						case COMMENT:
							TRACESWRITER.debug("recurseDefine !!! " + currentNode.getNodeName() + " in context "
									+ context + " on defineNode " + defineNodeName + " cancelled");
							break;
						case RNG_VALUE:
							dataString = currentNode.getTextContent();
							break;
						case RNG_ATTRIBUTE:
							if (currentNode.hasChildNodes()) {
								String value = "";
								String attrName = "";
								currentNodeList = currentNode.getChildNodes();
								for (int j = 0; j < currentNodeList.getLength(); j++) {
									attrNode = currentNodeList.item(j);
									attrName = currentNode.getAttributes().getNamedItem(ITEM_NAME).getNodeValue();
									if (RNG_VALUE.equals(attrNode.getNodeName())) {
										value = attrNode.getTextContent();
									} else if (RNG_DATA.equals(attrNode.getNodeName())) {
										if (currentDocumentTypeId != null && context.endsWith(CONTEXT_END_DOCATT)
												&& ATTR_NAME_FILENAME.equals(attrName)) {
											value = archiveDocuments.getFileName();
											TRACESWRITER.trace("recurseDefine generating filename '" + value
													+ "' for DOCLIST '" + currentDocumentTypeId + "'");
										}
										if (context.endsWith(CONTEXT_END_INTEGRITY)
												&& ATTR_NAME_ALGORITHME.equals(attrName)) {
											value = getCurrentDocumentHashAlgorithm();
										}
									}
									try {
										if (currentPass == 2) {
											elementCourantW.setAttribute(attrName, value);
										}
									} catch (DOMException e) {
										throw new TechnicalException("Impossible d'écrire l'attribut : " + attrName
												+ " avec la valeur " + value + " dans le contexte " + context + " : "
												+ e.getLocalizedMessage(), e);
									}
								}
							}
							break;
						case RNG_DATA:
							dataString = getData(context, currentNode);
							break;
						default:
							if (currentPass == 1) {
								logAndAddErrorsList("recurseDefine   ----  !!!! currentNode.Name '"
										+ currentNode.getNodeName() + "' Unhandled"); // En
																						// passe
																						// 1,
																						// cette
																						// erreur
																						// ne
																						// doit
																						// pas
																						// bloquer
																						// la
																						// recherche
																						// d'autres
																						// erreurs.
							} else {
								throw new TechnicalException("recurseDefine   ----  !!!! currentNode.Name '"
										+ currentNode.getNodeName() + "' Unhandled");
							}
							break;
						}
					}
					if (dataString != null) {
						if (currentPass == 2)
							elementCourantW.setTextContent(dataString);
					}
				}
			} else {
				throw new TechnicalException("defineNode '" + defineNodeName + "' nul et context : '" + context + "'");
			}
		} catch (XPathExpressionException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		} catch (DOMException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

		return currentDocumentTypeId;
	}

	/**
	 * Écriture des balises d'un rng:element Si le contenu fait appel à des rng:define peut appeler recurseDefine
	 */
	private void genElement(Node elemNode, String tagToWrite, String context) throws TechnicalException {
		String dataString = null;
		int counter = 1;
		boolean callNextDocument = false;
		NodeList elemNodeList = null;
		Node node = null;
		Element elt = null;
		if (NOEUD_DOCUMENT.equals(tagToWrite)) {
			if (currentDocumentTypeId != null) {
				String typeId = currentContainsNode.getRelativeContext();
				String documentIdentification = lookupForDocumentIdentification(elemNode);
				boolean withDocumentIdentification = StringUtils.isNotEmpty(documentIdentification);
				if (withDocumentIdentification) {
					typeId += "{" + documentIdentification + "}";
				}
				counter = archiveDocuments.prepareListForType(typeId, withDocumentIdentification);
				TRACESWRITER.trace("genElement Document : typeId " + typeId + " has " + counter + " documents");
				callNextDocument = true;
			}
		}
		for (int bcl = 0; bcl < counter; ++bcl) {
			if (callNextDocument)
				archiveDocuments.nextDocument();
			TRACESWRITER.trace("genElement (" + elemNode.getNodeName() + ", " + tagToWrite + ", " + context + ")");
			try {
				if (currentPass == 2) {
					if (NOEUD_ARCHIVETRANSFER.equals(tagToWrite)) {
						// SEDA 1.0 "fr:gouv:culture:archivesdefrance:seda:v1.0"
						// SEDA 0.2
						// "fr:gouv:ae:archive:draft:standard_echange_v0.2"
						String ns;
						ns = "";
						ns = grammarNode.getAttributes().getNamedItem("ns").getNodeValue();
						elt = docOut.createElement(tagToWrite);
						elementCourantW = elt;
						elt.setAttribute(XMLNS, ns);
						docOut.appendChild(elt);
					} else {
						elt = docOut.createElement(tagToWrite);
						elementCourantW.appendChild(elt);
						elementCourantW = elt;
					}
				}
			} catch (DOMException e) {
				throw new TechnicalException("Impossible d'écrire le tag " + tagToWrite + " pour le contexte "
						+ context + " : " + e.getLocalizedMessage(), e);
			}
			if (elemNode.hasChildNodes()) {
				elemNodeList = elemNode.getChildNodes();
				for (int i = 0; i < elemNodeList.getLength(); i++) {
					node = elemNodeList.item(i);
					switch (node.getNodeName()) {
					case RNG_REF:
						String newContext = context + "/" + tagToWrite;
						if (node.hasAttributes()) {
							if (NODE_VALUE_ANY_ELEMENT.equals(node.getAttributes().getNamedItem(ITEM_NAME)
									.getNodeValue())) {
								dataString = getTag(tagToWrite, context);
							} else {
								recurseDefine(node.getAttributes().getNamedItem(ITEM_NAME).getNodeValue(), newContext);
							}
						}
						break;
					case RNG_VALUE:
						dataString = node.getTextContent();
						break;
					case RNG_DATA:
						dataString = getTag(tagToWrite, context);
						break;
					default:
						TRACESWRITER.trace("genElement  ----  !!!! currentNode.Name Unhandled '" + node.getNodeName()
								+ "' in context '" + context + "' for tag2write '" + tagToWrite + "'");
						break;
					}
				}
				try {
					if (dataString != null) {
						if (currentPass == 2) {
							elementCourantW.setTextContent(dataString);
						}
					}
				} catch (DOMException e) {
					throw new TechnicalException("Impossible d'écrire le tag " + tagToWrite + " pour le contexte "
							+ context + " : " + e.getLocalizedMessage(), e);
				}
				if (currentPass == 1) {
					// On redescend dans l'arbre des ContainsNode
					// en passe 2 on utilise la méthode next qui permet de
					// parcurir l'arbre comme un vecteur
					if ((NOEUD_CONTAINS.equals(tagToWrite) // SEDA 0.2
							|| NOEUD_ARCHIVE_OBJECT.equals(tagToWrite) || NOEUD_ARCHIVE.equals(tagToWrite)) // SEDA
																											// 1.0
							&& currentContainsNode != rootContainsNode)
						currentContainsNode = currentContainsNode.getParent();
				}
				if (currentPass == 2) {
					currentNodeW = elementCourantW;
					do {
						currentNodeW = currentNodeW.getParentNode();
					} while (!(currentNodeW instanceof Element) && currentNodeW != null);
					elementCourantW = (Element) currentNodeW;
				}
			}
		}
	}

	/**
	 * Cette méthode recherche un rng:attribut du nœud désigné et retourne la valeur de rng:value ou null si l'attribut
	 * ou la valeur n'ont pas été trouvées Arborescence exemple <rng:define name="Size_N66177"> <!-- On est ici, on
	 * cherche du rng:attribute avec un name == attributeName -> <rng:data type="string"/> <rng:attribute
	 * name="unitCode"> <rng:value>2P</rng:value> </rng:attribute> </rng:define>
	 */
	private String lookupForAttribute(String attributeName, Node node) throws TechnicalException {
		XPath xPath = null;
		NodeList firstNodeList = null;
		Node firstStep;
		String defineNameToTest = node.getAttributes().getNamedItem(ITEM_NAME).getNodeValue();
		String value = null;
		xPath = XPathFactory.newInstance().newXPath();
		try {
			firstNodeList = (NodeList) xPath.evaluate("/grammar/define[@name='" + defineNameToTest
					+ "']/attribute[@name='" + attributeName + "']/value", docIn.getDocumentElement(),
					XPathConstants.NODESET);
			if (firstNodeList.getLength() > 0) {
				firstStep = firstNodeList.item(0);
				if (firstStep != null) {
					value = firstStep.getTextContent();
				}
			}
		} catch (XPathExpressionException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}
		return value;
	}

	/**
	 * Traitement associé aux balises rng:data nodeName est utile seulement pour tracer les actions et situer le
	 * contexte
	 */
	private String getData(String context, Node node) throws TechnicalException {

		String dataString = null;
		if (currentPass == 1) {
			dataString = "";
		} else {
			switch (context) {
			// case "/ArchiveTransfer/Archive/ContentDescription/Size": // SEDA
			// 1.0 Toutefois cette balise n'existe pas en SEDA 1.0
			case "/ArchiveTransfer/Contains/ContentDescription/Size": // SEDA
																		// 0.2
			{
				double sizeOfDocuments = 0;
				archiveDocuments.prepareCompleteList();
				while (archiveDocuments.nextDocument()) {
					long taille = getCurrentDocumentSize();
					sizeOfDocuments += taille;
				}
				TRACESWRITER.trace("Size computed = '" + sizeOfDocuments + "'");
				dataString = convertUnit(sizeOfDocuments, node);
			}
				break;
			case "/ArchiveTransfer/TransferIdentifier":
				dataString = sae.getTransferId();
				break;
			case "/ArchiveTransfer/Archive/Name": // SEDA 1.0
			case "/ArchiveTransfer/Contains/Name": // SEDA 0.2
				dataString = archiveDocuments.getKeyValue(KEY_TRANSFERNAME);
				break;
			case "/ArchiveTransfer/Comment":
				dataString = archiveDocuments.getKeyValue(KEY_COMMENT);
				break;
			case "/ArchiveTransfer/TransferringAgency/Description":
				dataString = sae.getTransferringAgencyDesc();
				break;
			case "/ArchiveTransfer/TransferringAgency/Identification":
				dataString = sae.getTransferringAgencyId();
				break;
			case "/ArchiveTransfer/TransferringAgency/Name":
				dataString = sae.getTransferringAgencyName();
				break;
			case "/ArchiveTransfer/ArchivalAgency/Description":
				dataString = sae.getArchivalAgencyDesc();
				break;
			case "/ArchiveTransfer/ArchivalAgency/Identification":
				dataString = sae.getArchivalAgencyId();
				break;
			case "/ArchiveTransfer/ArchivalAgency/Name":
				dataString = sae.getArchivalAgencyName();
				break;
			case "/ArchiveTransfer/Archive/ContentDescription/OriginatingAgency/BusinessType": // SEDA
																								// 1.0
			case "/ArchiveTransfer/Archive/ContentDescription/OriginatingAgency/Identification":
			case "/ArchiveTransfer/Archive/ContentDescription/OriginatingAgency/Description":
			case "/ArchiveTransfer/Archive/ContentDescription/OriginatingAgency/LegalClassification":
			case "/ArchiveTransfer/Archive/ContentDescription/OriginatingAgency/Name":
			case "/ArchiveTransfer/Contains/ContentDescription/OriginatingAgency/BusinessType": // SEDA
																								// 0.2
			case "/ArchiveTransfer/Contains/ContentDescription/OriginatingAgency/Identification":
			case "/ArchiveTransfer/Contains/ContentDescription/OriginatingAgency/Description":
			case "/ArchiveTransfer/Contains/ContentDescription/OriginatingAgency/LegalClassification":
			case "/ArchiveTransfer/Contains/ContentDescription/OriginatingAgency/Name":
				int posLastSlash = context.lastIndexOf('/') + 1;
				String balise = context.substring(posLastSlash, context.length());
				dataString = archiveDocuments.getKeyValue(KEY_ORIGINATINGAGENCY + KEY_TAG_SEPARATOR + balise);
				break;
			case "/ArchiveTransfer/Archive/ContentDescription/CustodialHistory": // SEDA 1.0
			case "/ArchiveTransfer/Contains/ContentDescription/CustodialHistory": // SEDA 0.2
				dataString = archiveDocuments.getKeyValue(KEY_CUSTODIALHISTORY);
				break;
			case "/ArchiveTransfer/Archive/ContentDescription/Description": // SEDA 1.0
			case "/ArchiveTransfer/Contains/ContentDescription/Description": // SEDA 0.2
				dataString = archiveDocuments.getKeyValue(KEY_CONTENTDESCRIPTION + KEY_TAG_SEPARATOR + KEY_DESCRIPTION);
				break;
			case "/ArchiveTransfer/Archive/ContentDescription/Keyword/KeywordContent": // SEDA
																						// 1.0
			case "/ArchiveTransfer/Contains/ContentDescription/ContentDescriptive/KeywordContent": // SEDA
																									// 0.2
				String keywordReferenceSchemeID = null;
				keywordReferenceSchemeID = getKeywordReferenceSchemeId(node, context);
				if (multipleSearch) {
					dataString = archiveDocuments.getNextKeyValue(KEY_KEYWORDCONTENT, null, keywordReferenceSchemeID);
					// multipleSearch = false;
				} else {
					dataString = archiveDocuments.getKeyValue(KEY_KEYWORDCONTENT
							+ CsvArchiveDocuments.BEGINNING_TAG_CAR + CsvArchiveDocuments.BEGINNING_TYPE_CAR
							+ keywordReferenceSchemeID + CsvArchiveDocuments.END_TYPE_CAR
							+ CsvArchiveDocuments.END_TAG_CAR);
				}
				break;
			case "/ArchiveTransfer/Archive/ArchiveObject/TransferringAgencyObjectIdentifier": // SEDA
																								// 1.0
			case "/ArchiveTransfer/Contains/Contains/TransferringAgencyObjectIdentifier": // SEDA
																							// 0.2
				dataString = "TODO: '" + context + "'";
				break;
			default:
				if (context.endsWith(CONTEXT_END_INTEGRITY)) {
					dataString = getCurrentDocumentHash();
				} else if (context.endsWith(CONTEXT_DOCUNIT_SIZE)) { // SEDA 0.2
					double sizeOfDocuments = 0;
					archiveDocuments.prepareListForType(currentContainsNode.getRelativeContext(), false);
					while (archiveDocuments.nextDocument()) {
						long taille = getCurrentDocumentSize();
						sizeOfDocuments += taille;
					}
					TRACESWRITER.trace("Size computed = '" + sizeOfDocuments + "'");
					dataString = convertUnit(sizeOfDocuments, node);
				} else if (context.endsWith(CONTEXT_END_DOCSIZE)) {
					double sizeOfDocuments = 0;
					long taille = getCurrentDocumentSize();
					sizeOfDocuments += taille;
					TRACESWRITER.trace("Size computed = '" + sizeOfDocuments + "'");
					dataString = convertUnit(sizeOfDocuments, node);
				} else if (context.endsWith("/Document/Description")) {
					dataString = archiveDocuments.getName();
				} else if (context.endsWith("/ArchiveObject/Name") // SEDA
																	// 1.0
						|| context.endsWith("/Contains/Contains/Name")) { // SEDA
																			// .02
					dataString = archiveDocuments.getKeyValue(KEY_CONTAINSNAME + CsvArchiveDocuments.BEGINNING_TAG_CAR
							+ currentContainsNode.getRelativeContext() + CsvArchiveDocuments.END_TAG_CAR);
				} else if (context.endsWith("/ArchiveObject/ArchivalAgencyObjectIdentifier") // SEDA
																								// 1.0
						|| context.endsWith("/Contains/ArchivalAgencyObjectIdentifier") // SEDA
																						// 0.2
						|| context.endsWith("/Document/Identification")) {
					objectIdentifier++;
					dataString = sae.getTransferId() + "_" + String.format("%05d", objectIdentifier);
				} else if (context.endsWith("ArchiveObject/ContentDescription/Keyword/KeywordContent") // SEDA
																										// 1.0
						|| context.endsWith("Contains/ContentDescription/ContentDescriptive/KeywordContent")) { // SEDA
																												// 0.2
					String keywordReferenceSchemeIDDefault = null;
					keywordReferenceSchemeIDDefault = getKeywordReferenceSchemeId(node, context);
					if (multipleSearch) {
						dataString = archiveDocuments.getNextKeyValue(KEY_KEYWORDCONTENT,
								ROOT.equals(currentDocumentTypeId) ? null : currentContainsNode.getRelativeContext(),
								keywordReferenceSchemeIDDefault);
						// multipleSearch = false;
					} else {
						dataString = archiveDocuments.getKeyValue(KEY_KEYWORDCONTENT
								+ CsvArchiveDocuments.BEGINNING_TAG_CAR + currentContainsNode.getRelativeContext()
								+ CsvArchiveDocuments.BEGINNING_TYPE_CAR + keywordReferenceSchemeIDDefault
								+ CsvArchiveDocuments.END_TYPE_CAR + CsvArchiveDocuments.END_TAG_CAR);
					}

				} else if (context.endsWith("ArchiveObject/ContentDescription/CustodialHistory") // SEDA
						// 1.0
						|| context.endsWith("Contains/ContentDescription/CustodialHistory")) { // SEDA
					// 0.2
					dataString = archiveDocuments.getKeyValue(KEY_CUSTODIALHISTORY
							+ CsvArchiveDocuments.BEGINNING_TAG_CAR + currentContainsNode.getRelativeContext()
							+ CsvArchiveDocuments.END_TAG_CAR);
				} else if (context.endsWith("ArchiveObject/ContentDescription/Description") // SEDA
						// 1.0
						|| context.endsWith("Contains/ContentDescription/Description")) { // SEDA
					// 0.2
					dataString = archiveDocuments.getKeyValue(KEY_CONTENTDESCRIPTION + KEY_TAG_SEPARATOR + KEY_DESCRIPTION + CsvArchiveDocuments.BEGINNING_TAG_CAR
							+ currentContainsNode.getRelativeContext() + CsvArchiveDocuments.END_TAG_CAR);
				} else if (context.endsWith("ContentDescription/FilePlanPosition") // SEDA
						// 1.0
						|| context.endsWith("Contains/ContentDescription/FilePlanPosition")) { // SEDA
					// 0.2

					Node defineFilePlanPosition;
					String filePlanPositionSchemeId = StringUtils.EMPTY;
					StringBuilder tag = new StringBuilder();

					defineFilePlanPosition = node.getParentNode();
					filePlanPositionSchemeId = lookupForAttribute(ATTR_NAME_SCHEME_ID, defineFilePlanPosition);

					if (!currentDocumentTypeId.equals(ROOT)) {
						tag.append(currentContainsNode.getRelativeContext());
					}

					if (StringUtils.isNotEmpty(filePlanPositionSchemeId)) {
						// dataString = archiveDocuments.getNextKeyValue(KEY_FILEPLANPOSITION, tag.toString(),
						// filePlanPositionSchemeId);
						if (multipleSearch) {
							dataString = archiveDocuments.getNextKeyValue(KEY_FILEPLANPOSITION, tag.toString(),
									filePlanPositionSchemeId);
							// multipleSearch = false;
						} else {
							dataString = archiveDocuments.getKeyValue(KEY_FILEPLANPOSITION
									+ CsvArchiveDocuments.BEGINNING_TAG_CAR + tag.toString()
									+ CsvArchiveDocuments.BEGINNING_TYPE_CAR + filePlanPositionSchemeId
									+ CsvArchiveDocuments.END_TYPE_CAR + CsvArchiveDocuments.END_TAG_CAR);
						}
					}
				} else {
					TRACESWRITER.trace("getData  ----  !!!! context '" + context + "' Unhandled in '"
							+ node.getNodeName() + "'");
				}
				break;
			}
		}
		return dataString;
	}

	/**
	 * Cette méthode permet, à partir d'un noeud rng:data correspondant à un keyword, d'aller chercher un éventuel
	 * schemeID du KeywordReference correspondant
	 *
	 * @param dataNode : noeud rng:data du mot-clé
	 * @return String schemeId ou "" si non trouvé.
	 */
	private String getKeywordReferenceSchemeId(Node dataNode, String context) throws TechnicalException {
		String result;
		Node parentDefineNode;
		String nameParentDefineNode;
		XPath xPath;
		NodeList refNodes;
		Node refNodeKeywordContent;
		Node defineContentDescriptive;
		String defineContentDescriptiveName;
		NodeList defineContentDescriptiveKWCNodes;
		Node keywordReferenceRefNode;
		String keywordReferenceRefName;
		NodeList keywordReferenceNodes;
		Node valueNode;

		nameParentDefineNode = "";
		result = null;
		xPath = XPathFactory.newInstance().newXPath();
		defineContentDescriptiveName = "";

		try {
			parentDefineNode = dataNode.getParentNode();
			if (parentDefineNode.hasAttributes()) {
				// Récupération du nom du noeu define parent
				nameParentDefineNode = parentDefineNode.getAttributes().getNamedItem(ITEM_NAME).getNodeValue();
				if (StringUtils.isNotEmpty(nameParentDefineNode)) {
					// Recherche du rng:ref de nom nameParentDefineNode
					refNodes = (NodeList) xPath.evaluate("/grammar//ref[@name='" + nameParentDefineNode + "']",
							docIn.getDocumentElement(), XPathConstants.NODESET);
					if (refNodes.getLength() > 0) {
						refNodeKeywordContent = refNodes.item(0);
						defineContentDescriptive = refNodeKeywordContent.getParentNode().getParentNode();
						if (defineContentDescriptive.hasAttributes()) {
							defineContentDescriptiveName = defineContentDescriptive.getAttributes()
									.getNamedItem(ITEM_NAME).getNodeValue();
							// On retrouve par requête XPath le schemeID du KeywordReference
							defineContentDescriptiveKWCNodes = (NodeList) xPath.evaluate("/grammar//define[@name='"
									+ defineContentDescriptiveName + "']//element[@name='KeywordReference']/ref",
									docIn.getDocumentElement(), XPathConstants.NODESET);
							if (defineContentDescriptiveKWCNodes.getLength() > 0) {
								keywordReferenceRefNode = defineContentDescriptiveKWCNodes.item(0);
								if (keywordReferenceRefNode.hasAttributes()) {
									keywordReferenceRefName = keywordReferenceRefNode.getAttributes()
											.getNamedItem(ITEM_NAME).getNodeValue();
									keywordReferenceNodes = (NodeList) xPath.evaluate("/grammar//define[@name='"
											+ keywordReferenceRefName + "']/attribute[@name='schemeID']/value",
											docIn.getDocumentElement(), XPathConstants.NODESET);
									if (keywordReferenceNodes.getLength() > 0) {
										valueNode = keywordReferenceNodes.item(0);
										result = valueNode.getFirstChild().getNodeValue();
									}
								}
							}
						}
					}

				}
			}
		} catch (XPathExpressionException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

		if (result != null) 
			return getDocumentTypeId(result, context);
		else {
			// On ne produit pas d'erreur ici, car on aura un message indiquant qu'un 
			// #KeywordContent est absent des données métier 
			return null;
		}
	}

	/**
	 * 
	 * @param sizeOfDocuments
	 * @param node
	 * @return la taille des documents passée en paramètre suivant l'unité UNITCODE du profil.
	 */
	private String convertUnit(double sizeOfDocuments, Node node) throws TechnicalException {
		String dataStringLocal;
		Node currentNodeUnitCode = node;
		do {
			currentNodeUnitCode = currentNodeUnitCode.getParentNode();
		} while (!(currentNodeUnitCode instanceof Element) && currentNodeUnitCode != null);
		String value = lookupForAttribute(UNITCODE_NAME, currentNodeUnitCode);
		if (value != null) {
			switch (value) {
			case UNITCODE_E36:
				sizeOfDocuments /= (1024 ^ 5);
				break; // petabyte
			case UNITCODE_E35:
				sizeOfDocuments /= (1024 ^ 4);
				break; // terabyte
			case UNITCODE_E34:
				sizeOfDocuments /= (1024 ^ 3);
				break; // gigabyte
			case UNITCODE_4L:
				sizeOfDocuments /= (1024 ^ 2);
				break; // megabyte
			case UNITCODE_2P:
				sizeOfDocuments /= 1024;
				break; // kilobyte
			case UNITCODE_AD:
				break;
			}
		}
		dataStringLocal = String.valueOf((long) (sizeOfDocuments + 0.5));
		return dataStringLocal;
	}

	/**
	 * fonction utilitaire utilisée par genElement
	 */
	private String getTag(String tag, String context) throws TechnicalException {
		if (currentPass == 1)
			return "";
		
		String dateString = null;
		String dateStringIn;
		SimpleDateFormat sdfBordereauZ = new SimpleDateFormat(FORMAT_DATE_BORDEREAU);
		StringBuilder errorMessage;

		switch (tag) {
		case TAG_RECEIPT:
			throw new RuntimeException("TODO: Receipt");

		case TAG_TYPE:
			throw new RuntimeException("TODO: Type");

		case TAG_ISSUE:
			throw new RuntimeException("TODO: Issue");

		case TAG_DURATION:
			throw new RuntimeException("TODO: Duration");

		case TAG_CREATION:
			dateStringIn = archiveDocuments.getDocumentDate();
			if ( ! "".equals(dateStringIn)) {
				try {
					dateString = tryParseDateDifferentFormat(dateStringIn, tag, context);
				} catch (TechnicalException e) {
					dateString = new StringBuilder().append("#DATAERR: date ").append(dateStringIn).toString();
					errorMessage = new StringBuilder()
							.append("#DATAERR: La date '")
							.append(dateStringIn)
							.append("' du document '")
							.append(archiveDocuments.getFileName())
							.append("' ne correspond pas à une date réelle ou son format est incorrect. Format attendu JJ/MM/AAAA hh:mm:ss");
					logAndAddErrorsList(errorMessage.toString());
					// throw new TechnicalException(errorMessage.toString(), e);
				}				
			} else { // Date non fournie
                try {
                    Path file = Paths.get(SAE_FilePath + "/" + archiveDocuments.getFileName());
                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

                    FileTime date = attr.lastModifiedTime();
                    dateString = tryParseDateDifferentFormat(date.toString(), tag, context);
                } catch (IOException e) { // on se contente de ne pas calculer
                    
                }
            }
			break;

		case TAG_OLDESTDATE:
			dateStringIn = archiveDocuments.getOldestDate();
			try {
				dateString = tryParseDateDifferentFormat(dateStringIn, tag, context);
				dateString = dateString.substring(0, dateString.indexOf("T"));
			} catch (TechnicalException e) {
				dateString = new StringBuilder().append("#DATAERR: date ").append(dateStringIn).toString();
				errorMessage = new StringBuilder()
						.append("#DATAERR: La date '")
						.append(dateStringIn)
						.append("' '")
						.append(tag)
						.append("' ne correspond pas à une date réelle ou son format est incorrect. Format attendu JJ/MM/AAAA hh:mm:ss");
				logAndAddErrorsList(errorMessage.toString());
				// throw new TechnicalException(errorMessage.toString(), e);
			}

			break;

		case TAG_STARTDATE:

		case TAG_LATESTDATE:
			dateStringIn = archiveDocuments.getLatestDate();
			try {
				dateString = tryParseDateDifferentFormat(dateStringIn, tag, context);
				dateString = dateString.substring(0, dateString.indexOf("T"));
			} catch (TechnicalException e) {
				dateString = new StringBuilder().append("#DATAERR: date ").append(dateStringIn).toString();
				errorMessage = new StringBuilder()
						.append("#DATAERR: La date '")
						.append(dateStringIn)
						.append("' '")
						.append(tag)
						.append("' ne correspond pas à une date réelle ou son format est incorrect. Format attendu JJ/MM/AAAA hh:mm:ss");
				logAndAddErrorsList(errorMessage.toString());
				// throw new TechnicalException(errorMessage.toString(), e);
			}

			break;

		case TAG_DATE:
			dateString = processTagDate(tag, context, sdfBordereauZ);
			break;

		case TAG_INTEGRITY:

			if (currentPass == 2) {
				switch (context) {
				// À partir de la version 1.0 du SEDA, la balise Integrity
				// est
				// dans Document
				default:
					break;
				// Version 0.2 du SEDA : la balise Integrity se trouve
				// au niveau
				// de ArchiveTransfer
				// et son contenu n'est pas précisé
				case "/ArchiveTransfer":
					/*
					 * <Contains algorithme="http://www.w3.org/2000/09/xmldsig#sha1" >
					 * 52a354f92d4d8a1e1c714ec6cd6a6f1ae51f4a14</Contains > <UnitIdentifier
					 * >056-225600014-20130924-0000008888-DE-1-1_1. PDF</UnitIdentifier>
					 */
					// C'est pas très propre, mais s'il y a plus d'un
					// document,
					// on doit générer la balise "Integrity"
					// autant de fois qu'il y a de documents, MAIS on
					// doit tenir
					// compte du fait que cette balise
					// est générée une fois par le code appelant...
					int nbDocuments = archiveDocuments.prepareCompleteList();
					int curDocument = 0;
					while (archiveDocuments.nextDocument()) {

						elementCourantW = createChildElement(NOEUD_CONTAINS, elementCourantW, docOut);
						elementCourantW.setAttribute(ATTR_NAME_ALGORITHME,
								getCurrentDocumentHashAlgorithm());
						elementCourantW.setTextContent(getCurrentDocumentHash());
						currentNodeW = getParentElementFromNode(elementCourantW);

						elementCourantW = (Element) currentNodeW;
						elementCourantW = createChildElement(NOEUD_UNITIDENTIFIER, elementCourantW, docOut);
						elementCourantW.setTextContent(archiveDocuments.getFileName()); // TEMP?
						currentNodeW = getParentElementFromNode(elementCourantW);

						elementCourantW = (Element) currentNodeW;
						curDocument++;
						if (curDocument < nbDocuments) {

							currentNodeW = getParentElementFromNode(elementCourantW);
							elementCourantW = (Element) currentNodeW;
							elementCourantW = createChildElement(NOEUD_INTEGRITY, elementCourantW, docOut);

						}
					}
					break;
				}
			}
			dateString = null;
			break;

		default:
			dateString = null;
			TRACESWRITER.trace("getTag  ----  !!!! tag Unhandled '" + tag + "'");
			break;
		}

		return dateString;
	}

	/**
	 * 
	 * @param nodeType
	 * @param elementCourant
	 * @param docOut
	 * @return Renvoi l'élément fils nouvellement créé
	 */
	private Element createChildElement(String nodeType, Element elementCourant, Document docOut) {

		Element child = docOut.createElement(nodeType);
		elementCourant.appendChild(child);

		return child;
	}

	private Element getParentElementFromNode(Node currentNode) throws TechnicalException {

		Element parentElement = null;
		Node currentNodeLocal = currentNode;

		while ((currentNodeLocal.getParentNode() != null) && (parentElement == null)) {

			currentNodeLocal = currentNodeLocal.getParentNode();
			if (currentNodeLocal instanceof Element) {
				parentElement = (Element) currentNodeLocal;
			}
		}

		if (parentElement == null) {

			throw new TechnicalException("Pas de parent de type Element"); // Ne
																			// doit
																			// jamais
																			// arriver,
																			// sinon
																			// cela
																			// bloque
																			// la
																			// génération
																			// du
																			// bordereau.
		}

		return parentElement;
	}

	/**
	 * 
	 * @param tag
	 * @param context
	 * @param sdfBordereauZ
	 * @return null si context est différent de /ArchiveTransfer
	 */
	private String processTagDate(String tag, String context, SimpleDateFormat sdfBordereauZ) {

		String dateString = null;
		switch (context) {
		case "/ArchiveTransfer":
			dateString = sdfBordereauZ.format(new Date());
		default:
			TRACESWRITER.trace("getTag  ----  !!!! context '" + context + "' Unhandled '" + tag + "'");
			break;
		}

		return dateString;
	}

	private String tryParseDateDifferentFormat(String dateStringIn, String tag, String context)
			throws TechnicalException {
		String dateStringOut = "";
		Date date;
		SimpleDateFormat pattern;
		SimpleDateFormat sdfBordereau = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

		List<String> knownPatterns = new ArrayList<String>();
		knownPatterns.add("yyyy-MM-dd' 'HH:mm:ss");
		knownPatterns.add("dd/MM/yyyy' 'HH:mm:ss");
		knownPatterns.add("yyyy-MM-dd");
		knownPatterns.add("dd/MM/yyyy");
		for (String patternString : knownPatterns) {
			try {
				pattern = new SimpleDateFormat(patternString);
				date = pattern.parse(dateStringIn);
				dateStringOut = sdfBordereau.format(date);
				break;
			} catch (ParseException pe) {
			} // On essaie plusieurs patterns
		}
		if (StringUtils.isEmpty(dateStringOut)) {
			throw new TechnicalException(ERROR_PARSE_DATE);
		}
		return dateStringOut;
	}

	/**
	 * Computes the hash of the current document
	 */
	private String computeHash(String documentFileName) {
		TRACESWRITER.debug("Début du calcul de l'intégrité du fichier : " + documentFileName);
		String retour = "";
		File f = new File(SAE_FilePath + "/" + documentFileName);
		retour = FileHashSum.getHash(f, this, HashAlgorithm.SHA256);
		TRACESWRITER.debug("Fin du calcul de l'intégrité du fichier : " + documentFileName);
		return retour;
	}

	/**
	 * getHash récupère l'empreinte dans le fichier des données métier ou la calcule.
	 *
	 * @param documentFileName permet de calculer l'empreinte si nécessaire
	 * @return
	 */
	private String getCurrentDocumentHash() {
		String hash = null;

		hash = archiveDocuments.getDocumentHash();

		if (hash == null) {
			hash = computeHash(archiveDocuments.getFileName());
		}

		return hash;
	}
	

	/**
	 * getHashAlgorithm récupère l'algorithme de l'empreinte dans le fichier des données métier renvoie la valeur par
	 * défaut
	 *
	 * @param documentFileName permet de calculer l'empreinte si nécessaire
	 * @return
	 */
	private String getCurrentDocumentHashAlgorithm() {
		String hashAlgorithm = null;

		hashAlgorithm = archiveDocuments.getDocumentHashAlgorithm();

		if (hashAlgorithm == null) {
			hashAlgorithm = DEFAULT_ALGORITHM;
		}

		return hashAlgorithm;
	}

	/**
	 * getSize récupère la taille dans le fichier des données métier ou la calcule.
	 *
	 * @param documentFileName permet de calculer la taille si nécessaire
	 * @return
	 */
	private long getCurrentDocumentSize() {
		String stTaille = null;
		long taille = 0L;

		stTaille = archiveDocuments.getDocumentSize();

		if (stTaille != null) {
			try {
				taille = Long.parseLong(stTaille);
			}
			catch (NumberFormatException e) {
				stTaille = null;
			}
		}
		if (stTaille == null) {
			File f = new File(SAE_FilePath + "/" + archiveDocuments.getFileName());
			taille = f.length();
		}

		return taille;
	}
	

	private String formatContainsIdentifier(String containsIdentifier, int numeroTag) {
		// TODO?: containsIdentifier - '+' + '[' + numéro d'ordre + ']'
		return containsIdentifier.substring(0, containsIdentifier.length() - 1) + "[#" + String.valueOf(numeroTag)
				+ "]";
	}

	/**
	 * MJ : Rajout pour trace
	 * 
	 * @param node
	 * @param indent
	 */
	private static String printDomTree(Node node, String indent, String tree) {
		String type;
		switch (node.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			type = DT_ATTRIBUT;
			break;
		case Node.CDATA_SECTION_NODE:
			type = DT_CDATA;
			break;
		case Node.COMMENT_NODE:
			type = DT_COMMENT;
			break;
		case Node.DOCUMENT_FRAGMENT_NODE:
			type = DT_DOCUMENT_FRAGMENT;
			break;
		case Node.DOCUMENT_NODE:
			type = DT_DOCUMENT;
			break;
		case Node.DOCUMENT_TYPE_NODE:
			type = DT_DOCUMENT_TYPE;
			break;
		case Node.ELEMENT_NODE:
			type = DT_NODE;
			break;
		case Node.ENTITY_NODE:
			type = DT_ENTITY;
			break;
		case Node.ENTITY_REFERENCE_NODE:
			type = DT_ENTITY_REFERENCE;
			break;
		case Node.NOTATION_NODE:
			type = DT_NOTATION;
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			type = DT_PROCESSING_INSTRUCTION;
			break;
		case Node.TEXT_NODE:
			type = DT_TEXT;
			break;
		default:
			type = DT_NONE;
		}
		tree += "\n" + indent + "type : " + type;
		TRACESWRITER.trace(indent + "type : " + type);
		tree += "\n" + indent + "noeud name  : " + node.getNodeName();
		TRACESWRITER.trace(indent + "noeud name  : " + node.getNodeName());
		tree += "\n" + indent + "value : " + node.getNodeValue();
		TRACESWRITER.trace(indent + "value : " + node.getNodeValue());
		if (node.hasChildNodes()) {
			Node nextFils = node.getFirstChild();
			while (nextFils != null) {
				tree = printDomTree(nextFils, indent + " ", tree);
				nextFils = nextFils.getNextSibling();
			}
		}
		return tree;
	}

	/**
	 * Écrit dans un fichier un document DOM, étant donné un nom de fichier.
	 * 
	 * @param doc le document à écrire
	 * @param nomFichier le nom du fichier de sortie
	 */
	private static void ecrireDocument(Document doc, String nomFichier) throws TechnicalException {
		String tempFilename = nomFichier + PROCESSING_EXTENSION; // On renommera
																	// le
																	// fichier
																	// avec son
																	// vrai nom
																	// quand le
																	// traitement
																	// aura
																	// réussi
		File tempFile = new File(tempFilename);
		// on considère le document "doc" comme étant la source d'une
		// transformation XML
		Source source = new DOMSource(doc);
		// le résultat de cette transformation sera un flux d'écriture dans
		// un fichier

		Result resultat = new StreamResult(tempFile);
		try {
			resultat.setSystemId(java.net.URLDecoder.decode(resultat.getSystemId(), WRITING_ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}
		// création du transformateur XML
		Transformer transfo = null;
		TransformerFactory transfoFactory = null;
		try {
			transfoFactory = new org.apache.xalan.xsltc.trax.TransformerFactoryImpl();
			transfoFactory.setAttribute(TRANSFO_INDENT_NUMBER_ATTR, TRANSFO_INDENT_NUMBER_VALUE);
			transfo = transfoFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

		// configuration du transformateur

		// sortie en XML
		transfo.setOutputProperty(OutputKeys.METHOD, TRANSFO_METHOD);

		// inclut une déclaration XML (recommandé)
		transfo.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, TRANSFO_OMIT_XML_DECLARATION);

		// codage des caractères : UTF-8. Ce pourrait être également ISO-8859-1
		transfo.setOutputProperty(OutputKeys.ENCODING, TRANSFO_ENCODING);

		// idente le fichier XML
		transfo.setOutputProperty(OutputKeys.INDENT, TRANSFO_INDENT);
		try {
			transfo.transform(source, resultat);
		} catch (TransformerException e) {
			throw new TechnicalException(ERROR_TRANSFORMATION_FAILED + e.getLocalizedMessage(), e);
		}

		// renomme le fichier avec son nom final
		if (tempFile.exists()) {
			File newFileExpected = new File(nomFichier);
			if (newFileExpected.exists()) {
				boolean deleteSucceded = newFileExpected.delete();
				if (!deleteSucceded) {
					throw new TechnicalException(ERROR_DELETE + newFileExpected.getAbsolutePath());
				}
			}
			boolean renameSucceded = tempFile.renameTo(new File(nomFichier));
			if (!renameSucceded) {
				throw new TechnicalException(ERROR_RENAME_1 + tempFile.getAbsolutePath() + ERROR_RENAME_2 + nomFichier);
			}
		} else {
			throw new TechnicalException(ERROR_TEMP_DOESNT_EXIST_1 + tempFile.getAbsolutePath()
					+ ERROR_TEMP_DOESNT_EXIST_2);
		}
	}

	/**
	 * 
	 * Cette méthode permet : - de loguer dans slf4j une "error", sachant que ce log contient tous les messages de
	 * toutes les générations de bordereaux faites par le module - d'ajouter l'erreur à errorsList, pour ainsi créer un
	 * fichier d'erreurs associé à chacun des bordereaux, en général, on utilise cette méthode lorsque les erreurs
	 * rencontrées permettent de chercher d'éventuelles autres erreurs, mais de toute façon le bordereau ne sera pas
	 * générer si des erreurs existent.
	 */
	private void logAndAddErrorsList(String error) {
		TRACESWRITER.error(error);
		errorsList.add(error);
	}

	/**
	 * 
	 * @return la somme des erreurs de l'ArchiveDocuments et de errorsList
	 */
	private int getNbErrors() {
		int nbErrorsLocal = 0; // Somme des erreurs de cette classe et de
								// archiveDocuments
		ArrayList<String> archiveDocumentsErrorsListLocal = new ArrayList<String>();
		if (archiveDocumentsLoaded) {
			archiveDocumentsErrorsListLocal = archiveDocuments.getErrorsList();
		}
		nbErrorsLocal = errorsList.size() + archiveDocumentsErrorsListLocal.size();
		return nbErrorsLocal;
	}

	/**
	 * 
	 * @return toutes les erreurs
	 */
	public ArrayList<String> getErrorsList() {
		ArrayList<String> errorsListTotal = new ArrayList<String>();
		errorsListTotal.addAll(errorsList);
		if (archiveDocumentsLoaded) {
			errorsListTotal.addAll(archiveDocuments.getErrorsList());
		}
		TRACESWRITER.debug(TRACE_BEGIN_ERRORS_LIST);
		for (int i = 0; i < errorsListTotal.size(); i++) {
			TRACESWRITER.debug(errorsListTotal.get(i));
		}
		TRACESWRITER.debug(TRACE_END_ERRORS_LIST);
		return errorsListTotal;
	}

	/**
	 * Si des erreurs existent, on les écrit dans le fichier passé en paramètre
	 * 
	 * @param filenameOutput
	 */
	public boolean writeErrorsIfExist(String filenameOutput) throws TechnicalException {
		boolean existErrors = false;
		try {
			FileWriter writer = new FileWriter(filenameOutput);
			ArrayList<String> errorsListLocal = getErrorsList();
			if (errorsListLocal.size() > 0) {
				existErrors = true;
				for (String str : errorsListLocal) {
					writer.write(str + "\n");
				}
			} else {
				existErrors = false;
				TRACESWRITER.trace(TRACE_NO_ERROR_TO_WRITE + filenameOutput);
			}
			writer.close();
			TRACESWRITER.trace(ERRORS_IN_FILE + filenameOutput);
		} catch (IOException e) {
			throw new TechnicalException(ERRORS_UNABLE_TO_WRITE_IN_FILE + filenameOutput + " : "
					+ e.getLocalizedMessage(), e);
		}
		return existErrors;
	}

	public void addErrorsList(String error) {
		errorsList.add(error);
	}

	public SedaSummaryRngGeneratorDaoInterface getSedaSummaryRngGeneratorDao() {
		return sedaSummaryRngGeneratorDao;
	}

	public void setSedaSummaryRngGeneratorDao(SedaSummaryRngGeneratorDaoInterface sedaSummaryRngGeneratorDao) {
		this.sedaSummaryRngGeneratorDao = sedaSummaryRngGeneratorDao;
	}

	/**
	 * Permet de récupérer le premier noeud enfant ayant la balise souhaitée (non récursif)
	 *
	 * @param node Noeud à partir duquel chercher un enfant
	 * @param childTag Nom de la balise de l'enfant cherché
	 * @param name Permet de chercher une balise avec le nom renseigné, si name est null, alors il est ignoré et le
	 *            premier enfant correspondant à la balise est renvoyé
	 * @return Node Noeud si trouvé, null sinon
	 */
	private Node getFirstChildNodeByName(Node node, String childTag, String name) {
		NodeList childrenList;
		Node currentChild;
		Node childFound;

		childrenList = node.getChildNodes();
		childFound = null;

		for (int i = 0; i < childrenList.getLength(); i++) {
			currentChild = childrenList.item(i);
			if (currentChild.getNodeName().equals(childTag)) {
				if (name != null) {
					if (currentChild.hasAttributes()
							&& name.equals(currentChild.getAttributes().getNamedItem(ATTR_NAME_NAME).getNodeValue())) {
						childFound = currentChild;
						break;
					}
				} else {
					childFound = currentChild;
					break;
				}
			}
		}

		return childFound;
	}

	/**
	 * A partir d'un Node, permet de récupérer la valeur d'un attribut
	 *
	 * @param node Le noeud dont on veut récupérer un attribut
	 * @param attributeName Le nom de l'attribut
	 * @return la valeur de l'attribut, null si non trouvé
	 */
	private String getAttributeFromNode(Node node, String attributeName) {
		String result;

		result = null;

		if (node.hasAttributes()) {
			result = node.getAttributes().getNamedItem(attributeName).getNodeValue();
		}

		return result;
	}

	/**
	 * Permet de récupérer, à partir de l'élément rng:element name="ContentDescriptive" le noeud rng:data du
	 * KeywordContent, ce noeud permet ensuite de récupérer l'ID.
	 *
	 * @param contentDescriptiveElementNode Noeud à partir duquel on chercher rng:data
	 * @return Node noeud rng:data, null si non trouvé
	 */
	private Node getDataKeywordNodeFromContentDescriptiveNode(Node contentDescriptiveElementNode)
			throws TechnicalException {
		Node resultData;
		Node refChildContentDescriptive;
		String refChildContentDescriptiveName;
		Node defineContentDescriptive;
		Node elementKeywordContent;
		Node refKeywordContent;
		String refKeywordContentName;
		Node defineKeywordContent;

		resultData = null;
		refChildContentDescriptive = null;
		refChildContentDescriptiveName = null;
		defineContentDescriptive = null;
		elementKeywordContent = null;
		refKeywordContent = null;
		refKeywordContentName = null;
		defineKeywordContent = null;

		// 1 Récupération nom du ref du ContentDescriptive
		if (contentDescriptiveElementNode != null) {
			refChildContentDescriptive = getFirstChildNodeByName(contentDescriptiveElementNode, RNG_REF, null);
		}
		if (refChildContentDescriptive != null) {
			refChildContentDescriptiveName = getAttributeFromNode(refChildContentDescriptive, ATTR_NAME_NAME);
		}
		// 2 Récupération nom du ref du KeywordContent
		if (refChildContentDescriptiveName != null) {
			defineContentDescriptive = getDefineNodeByName(refChildContentDescriptiveName);
		}
		if (defineContentDescriptive != null) {
			elementKeywordContent = getFirstChildNodeByName(defineContentDescriptive, RNG_ELEMENT, KEY_KEYWORDCONTENT);
		}
		if (elementKeywordContent != null) {
			refKeywordContent = getFirstChildNodeByName(elementKeywordContent, RNG_REF, null);
		}
		if (refKeywordContent != null) {
			refKeywordContentName = getAttributeFromNode(refKeywordContent, ATTR_NAME_NAME);
		}
		// 3 Récupération du noeud rng:data correspondant au mot-clé
		if (refKeywordContentName != null) {
			defineKeywordContent = getDefineNodeByName(refKeywordContentName);
		}
		if (defineKeywordContent != null) {
			resultData = getFirstChildNodeByName(defineKeywordContent, RNG_DATA, null);
		}
		return resultData;
	}

	/**
	 * Cette méthode fait une requête XPath pour récupérer le noeud rng:define dont on donne le nom
	 *
	 * @param defineNodeName nom du define que nous voulons récupérer
	 * @return Noeud rng:define récupéré, null sinon
	 * @throws TechnicalException
	 */
	private Node getDefineNodeByName(String defineNodeName) throws TechnicalException {
		XPath xPath;
		NodeList defineContentDescriptiveNodes;
		Node defineResultNode;

		xPath = XPathFactory.newInstance().newXPath();
		defineContentDescriptiveNodes = null;
		defineResultNode = null;

		try {
			defineContentDescriptiveNodes = (NodeList) xPath.evaluate("/grammar//define[@name='" + defineNodeName
					+ "']", docIn.getDocumentElement(), XPathConstants.NODESET);
			if (defineContentDescriptiveNodes.getLength() > 0) {
				defineResultNode = defineContentDescriptiveNodes.item(0);
			}
		} catch (XPathExpressionException e) {
			throw new TechnicalException(e.getLocalizedMessage(), e);
		}

		return defineResultNode;
	}
}
