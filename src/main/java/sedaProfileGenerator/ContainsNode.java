package sedaProfileGenerator;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exception.TechnicalException;

/**
 * Cette classe permet de calculer le nombre de documents contenus dans les unités documentaires (filles comprises) Elle
 * implémente un arbre de ContainsNode Tout d'abord l'arbre doit être construit avec la méthode addNewNode incNbDocs
 * permet de mettre à jour le nombre de documents dans le nœud courant La méthode computeNbDocuments permet à la fin de
 * création de l'arbre de calculer le nombre de documents à chaque niveau de l'arborescence Ensuite getNbDocuments
 * permet de connaître le nombre de documents dans une branche La méthode next() permet de lire l'arbre comme un vecteur
 * pour la seconde passe La méthode getRelativeContext renvoie une chaîne contenant le nom du tag précédé éventuellement
 * des tag numérotés qui font partie de ses parents
 * */
public class ContainsNode {
	private static final Logger SLF4JLOGGER = LoggerFactory.getLogger(ContainsNode.class);
	private static final String LOT_END_TAG = "+";
	private static final String LOT_PATTERN = "\\[#(\\d+)\\]";
	private static final String LOT_PATTERN_BEGINNING = "[#";
	private static final String LOT_PATTERN_END = "]";
	private static final String RELATIVE_CONTEXT_PATTERN = ".*\\[#(\\d+)\\]";
	private static final String CONTEXT_SEPARATOR = "//";

	private int nbDocuments;
	private boolean mandatory; // indique que l'unité documentaire est
								// obligatoire
								// (contenue dans rng:oneOrMore ou spécifiée
								// dans rng:element
								// elle doit donc contenir un ou plusieurs
								// documents
								// si cette condition n'est pas remplie, une
								// alerte doit être levée
	private String objectIdentifier;
	private ArrayList<ContainsNode> childrens = new ArrayList<ContainsNode>();;
	private ContainsNode parent;

	/**
	 * Le premier nœud doit être créé avec un parentNode null
	 */
	public ContainsNode(String nodeId, ContainsNode parentNode, boolean bContainsIsMandatory) {
		objectIdentifier = nodeId;
		nbDocuments = 0;
		parent = parentNode;
		mandatory = bContainsIsMandatory;
	}

	public String getName() {
		return objectIdentifier;
	}

	public ContainsNode getParent() {
		return parent;
	}

	/**
	 * Ajoute un nœud au nœud courant
	 */
	public ContainsNode addNewNode(String newNodeId, boolean bContainsIsMandatory) throws TechnicalException {
		int numeroTag = 0;
		if (newNodeId.endsWith(LOT_END_TAG)) {
			numeroTag++;
			String nodeName = newNodeId.substring(0, (newNodeId.length() - 1));
			for (ContainsNode child : childrens) {
				// Le nom des frères peut commencer par nodeName
				// Le nom du frère est donc de la forme nodeName[numero]
				String brotherName = child.getName();
				Pattern p = Pattern.compile(nodeName + LOT_PATTERN);
				Matcher m = p.matcher(brotherName);
				if (m.matches()) {
					try {
						// int newNumeroTag = Integer.parseInt((m.group(1)) + 1);
						int newNumeroTag = Integer.parseInt((m.group(1))) + 1;
						if (newNumeroTag > numeroTag) {
							numeroTag = newNumeroTag;
						}
					} catch (Exception e) {
						throw new TechnicalException("Le tag " + newNodeId + " est mal formé : "
								+ e.getLocalizedMessage(), e); // Cette erreur ne
																// doit jamais
																// arriver puisque
																// nous savons que
																// si m.matches()
																// alors on a un
																// entier (d du
																// pattern)
					}
				}
			}
		}
		if (numeroTag > 0) {
			newNodeId = newNodeId.substring(0, newNodeId.length() - 1) + LOT_PATTERN_BEGINNING + numeroTag
					+ LOT_PATTERN_END;
		}
		ContainsNode newNode = new ContainsNode(newNodeId, this, bContainsIsMandatory);
		this.childrens.add(newNode);
		return newNode;
	}

	/**
	 * Utilisé à partir de la racine, permet de couper les branches vides Les branches sont coupées à partir des nœuds
	 * qui ont 0 document Seul le nœud qui a 0 document est conservé, ses enfants sont éliminés
	 */
	public void trunkEmptyBranches() {
		if (this.nbDocuments == 0) {
			childrens.clear();
		} else {

			for (ContainsNode node : this.childrens) {
				node.trunkEmptyBranches();
			}
		}
	}

	/**
	 * Utilisé à partir de la racine, permet de parcourir l'ensemble de l'arbre comme un vecteur. Retourne null si il
	 * n'y a plus d'éléments dans l'arbre
	 */
	public ContainsNode next() {
		ContainsNode containsNodeLocal = null;
		if (this.childrens.size() > 0) {
			containsNodeLocal = this.childrens.get(0); // on retourne le premier
														// fils
		} else {
			containsNodeLocal = this.nextBrother();
		}
		return containsNodeLocal;
	}

	private ContainsNode nextBrother() {
		ContainsNode containsNodeLocal = null;
		boolean callerReached = false;
		if (this.parent != null) {
			for (ContainsNode child : this.parent.childrens) {
				if (callerReached) {
					containsNodeLocal = child;
					break;
				}
				if (this.equals(child)) {
					callerReached = true;
				}
			}
		}
		if (containsNodeLocal == null) {
			if (parent != null) {
				containsNodeLocal = parent.nextBrother();
			}
		}
		return containsNodeLocal;
	}

	/**
	 * Permet de récupérer le chemin relatif du nœud courant retourne le nom du tag précédé des tags numérotés de ses
	 * ancètres les tags non numérotés sont exclus de cette liste exemple : /TAG_A[#1]/TAG_B/TAG_C~#2]/TAG_D/THIS_TAG a
	 * pour chemin relatif TAG_A[#1]//TAG_C~#2]//THIS_TAG
	 * 
	 * TODO: à optimiser, attribut chemin relatif, calcul des chemins relatifs déclenché par exemple à
	 * computeNbDocuments
	 */
	public String getRelativeContext() {
		String context = "";
		ContainsNode node = this;
		while ((node = node.getParent()) != null) {
			String parentName = node.getName();
			Pattern p = Pattern.compile(RELATIVE_CONTEXT_PATTERN);
			Matcher m = p.matcher(parentName);
			if (m.matches()) {
				context = parentName + CONTEXT_SEPARATOR + context;
			}
		}
		context += this.getName();
		return context;
	}

	/**
	 * Permet de mettre à jour le nombre de documents dans le nœud courant La méthode computeNbDocuments sera chargée en
	 * fin de création de l'arbre de calculer le nombre de documents à chque niveau de l'arborescence
	 */
	public void incNbDocs(int counter) {
		nbDocuments += counter;
	}

	/**
	 * Cette méthode récursive calcule et met à jour le nombre de documents contenus dans le nœud et ses enfants. Pour
	 * calculer le nombre total de documents, il faut l'exécuter sur le nœud racine
	 */
	public int computeNbDocuments() {
		for (ContainsNode node : childrens) {
			nbDocuments += node.computeNbDocuments();
		}
		return nbDocuments;
	}

	/**
	 * Cette méthode retourne le nombre de documents du nœud
	 */
	public int getNbDocuments() {
		return nbDocuments;
	}

	/**
	 * Cette méthode retourne l'attribut obligatoire du nœud
	 */
	public boolean getMandatory() {
		return mandatory;
	}

	public ArrayList<ContainsNode> getChildrens() {
		return childrens;
	}
}
