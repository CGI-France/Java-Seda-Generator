package sedaProfileGenerator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public final class Checker {
	private static final String DB_URL_PATTERN = "jdbc:postgresql://.*";

	private Checker() {

	}

	public static void checkString(String arg) {
		if (StringUtils.isEmpty(arg)) {
			throw new IllegalArgumentException("Un paramètre est nul ou vide.");
		}
	}

	public static void checkFile(String filename) {
		checkString(filename);
		File file = new File(filename);
		if (!file.exists()) {
			throw new IllegalArgumentException("Le fichier " + filename + " n'existe pas.");
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("Le fichier " + filename + " est illisible.");
		}
	}

	public static void checkFolder(String filename) {
		checkString(filename);
		File file = new File(filename);
		if (!file.exists()) {
			throw new IllegalArgumentException(filename + " n'existe pas.");
		}
		if (!file.isDirectory()) {
			throw new IllegalArgumentException(filename + " n'est pas un dossier.");
		}
	}

	public static void checkDbUrl(String url) {
		checkString(url);
		Pattern p = Pattern.compile(DB_URL_PATTERN);
		Matcher m = p.matcher(url);
		if (!m.matches()) {
			throw new IllegalArgumentException(url + " n'est pas de la forme : " + DB_URL_PATTERN);
		}
	}

	public static void checkParentFolder(String filename) {
		checkString(filename);
		File file = new File(filename);
		File parentFile = file.getParentFile();
		if (parentFile != null) {
			checkFolder(parentFile.getAbsolutePath());
		} // Sinon le fichier sera créé à la racine du lancement, donc le fichier pourra être créé.
	}

}
