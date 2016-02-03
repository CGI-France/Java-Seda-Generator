package unit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sedaProfileGenerator.Checker;
import exception.TechnicalException;

public class SedaGeneratorProperties {

	private static final Logger TRACESWRITER = LoggerFactory.getLogger(SedaGeneratorProperties.class);

	private static final String UTF8_BOM = "\uFEFF";
	private static final String ENCODAGE = "UTF8";
	private static final String SEPARATOR = "=";

	private static final String ERROR_NO_FILE_1 = "Le fichier ";
	private static final String ERROR_NO_FILE_2 = " n'a pas ete trouvé.";
	private static final String ERROR_READING = "Erreur lors de la lecture du fichier ";
	private static final String ERROR_FILE = "Fichier ";
	private static final String ERROR_LINE = ": erreur à la ligne ";
	private static final String ERROR_EQUAL = " = ";
	private static final String ERROR_ADDING = "On ajoute ";
	private static final String ERROR_PROPERTIES = " aux propriétés.";
	private static final String ERROR_PROPERTY_NOT_RECOGNIZED = ", la propriété n'est pas reconnue";
	private static final String ERROR_PROPERTY_FORMAT = ", format attendu property=value";

	private HashMap<String, String> properties = new HashMap<String, String>();

	public SedaGeneratorProperties(String propertyPath) throws TechnicalException {
		readProperties(propertyPath);
	}

	public String getProperty(String property) {
		return this.properties.get(property);
	}

	public void addProperty(String property, String value) {
		this.properties.put(property, value);
	}

	/**
	 * Lit le fichier de propriétés.
	 *
	 * @param propertyPath
	 * @throws TechnicalException
	 */
	public void readProperties(String propertyPath) throws TechnicalException {
		String rgxSeperator;
		String line;
		String[] elements;

		Checker.checkFile(propertyPath);
		try {
			File f = new File(propertyPath);
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, ENCODAGE);
			BufferedReader br = new BufferedReader(isr);
			int k = 1;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					line = removeUTF8BOM(line);
					rgxSeperator = SEPARATOR;
					elements = line.split(rgxSeperator);
					if (elements.length == 2) {
						if (SedaGeneratorPropertiesEnum.contains(elements[0])) {
							TRACESWRITER.debug(ERROR_ADDING + elements[0] + ERROR_EQUAL + elements[1]
									+ ERROR_PROPERTIES);
							this.properties.put(elements[0], elements[1]);
						} else {
							throw new TechnicalException(ERROR_FILE + f.getPath() + ERROR_LINE + k
									+ ERROR_PROPERTY_NOT_RECOGNIZED);
						}
					} else {
						throw new TechnicalException(ERROR_FILE + f.getPath() + ERROR_LINE + k + ERROR_PROPERTY_FORMAT);
					}
				}
				k++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			throw new TechnicalException(ERROR_NO_FILE_1 + propertyPath + ERROR_NO_FILE_2, e);
		} catch (IOException e) {
			throw new TechnicalException(ERROR_READING + propertyPath, e);
		}
	}

	private static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM)) {
			s = s.substring(1);
		}
		return s;
	}
}
