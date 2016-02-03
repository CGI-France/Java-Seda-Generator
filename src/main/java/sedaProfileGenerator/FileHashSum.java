package sedaProfileGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calcule les empreintes des fichiers
 */
public final class FileHashSum {
	private static final Logger TRACESWRITER = LoggerFactory.getLogger(FileHashSum.class);
	private static final char MSB = '0';
	private static final String FORMAT_BYTE = "%x";
	private static final int BUFFER_SIZE = 262144;

	private FileHashSum() {

	}

	/**
	 * Retourne l'empreinte sha1 de fichier <code>file</code> ou null si le fichier n'a pas pu être lu.
	 * 
	 * @param file le fichier dont on doit calculer l'empreinte sha1
	 * @return l'empreinte sha1
	 */
	public static String getHash(File file, SedaSummaryRngGenerator ssg, HashAlgorithm hashAlgorithm) {
		String localSha1Sum = null;
		if (file.exists() && file.isFile() && file.canRead()) {
			try {
				MessageDigest md = MessageDigest.getInstance(hashAlgorithm.getHashAlgorithmName());
				DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);
				dis.on(true);
				byte[] buffer = new byte[BUFFER_SIZE];
				int leftToRead = BUFFER_SIZE;

				// while (dis.read() != -1) {
				while ((leftToRead = dis.read(buffer, 0, leftToRead)) != -1) {
					;
				}
				dis.close();
				byte[] b = md.digest();
				localSha1Sum = getHexString(b);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			logAndAddErrorsList("impossible de trouver le fichier : " + file.getAbsolutePath(), ssg);
		}
		return localSha1Sum;
	}

	private static String getHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			if (b <= 0x0F && b >= 0x00) { // On rajoute le 0 de poid fort ignoré
											// à la conversion.
				sb.append(MSB);
			}
			sb.append(String.format(FORMAT_BYTE, b));
		}
		return sb.toString();
	}

	/**
	 * 
	 * Cette méthode permet : - de loguer dans slf4j une "error", sachant que ce log contient tous les messages de
	 * toutes les générations de bordereaux faites par le module - d'ajouter l'erreur à errorsList, pour ainsi créer un
	 * fichier d'erreurs associé à chacun des bordereaux.
	 */
	public static void logAndAddErrorsList(String error, SedaSummaryRngGenerator ssg) {
		TRACESWRITER.error(error);
		ssg.addErrorsList(error);
	}
}