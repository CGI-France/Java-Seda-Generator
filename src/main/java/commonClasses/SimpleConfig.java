package commonClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exception.TechnicalException;

public class SimpleConfig {
	private static final Logger TRACESWRITER = LoggerFactory.getLogger(SimpleConfig.class);

	private static final String BEGINNING_CAR = "#";
	private static final String SECTION_GENERATOR = "generator";
	private static final String SECTION_GENERATOR_AUTHORIZED_FILES = "trace|accord|data|rep_documents|baseURI|bordereau";
	private static final String AUTHORIZED_FILES_REGEX_BEGIN = "^\\s*(";
	private static final String AUTHORIZED_FILES_REGEX_END = ")\\s*=\\s*(([a-zA-Z0-9]|_|-|:|\\.|/)+)\\s*$";
	private static final String SECTION_REGEX = "^\\[([a-zA-z0-9]+)\\s*(:)+\\s*(([a-zA-z0-9]|-)+)\\]\\s*$";
	private static final String SUBSECTION_TRACE = "trace";
	private static final String SUBSECTION_ACCORD = "accord";
	private static final String SUBSECTION_DATA = "data";
	private static final String SUBSECTION_REP_DOCUMENTS = "rep_documents";
	private static final String SUBSECTION_BASE_URI = "baseURI";
	private static final String SUBSECTION_BORDEREAU = "bordereau";

	private static final String ENCODAGE = "UTF8";
	private static final String UTF8_BOM = "\uFEFF";

	private static final String INFO_CONFIG_LOAD = "Chargement du fichier de configuration : ";

	private static final String ERROR_CONFIG_SECTION_1 = "Le nom de section '";
	private static final String ERROR_CONFIG_SECTION_2 = "' n'existe pas.";
	private static final String ERROR_NO_FILE_1 = "Le fichier ";
	private static final String ERROR_NO_FILE_2 = " n'a pas ete trouvé.";
	private static final String ERROR_READING = "Erreur lors de la lecture du fichier ";

	private boolean traceActions = false;
	private ArrayList<String> errorsList;

	private ArrayList<GeneratorConfig> generatorList;

	private String section;
	private String sectionName;
	private String traceFile;
	private String profileFile;
	private String dataFile;
	private String repDocuments;
	private String baseURI;
	private String bordereauFile;
	private String accordVersement;
	private boolean inSection = false;

	public SimpleConfig() {
		errorsList = new ArrayList<String>();
		generatorList = new ArrayList<GeneratorConfig>();
	}

	/**
	 * Retourne la configuration demandée ou la première si le nom de config est vide
	 * 
	 * @param configName
	 * @return
	 */
	public GeneratorConfig getGeneratorConfig(String configName) {
		GeneratorConfig config = null;
		for (GeneratorConfig c : generatorList) {
			if (StringUtils.isEmpty(configName) || c.getNomJob().equals(configName)) {
				config = c;
				break;
			}
		}
		return config;
	}

	private void doSection() {
		if (inSection) {
			switch (section) {
			case SECTION_GENERATOR:
				GeneratorConfig generator = new GeneratorConfig();
				generator.nomJob = sectionName;
				generator.traceFile = traceFile;
				generator.accordVersement = accordVersement;
				generator.dataFile = dataFile;
				generator.repDocuments = repDocuments;
				generator.baseURI = baseURI;
				generator.bordereauFile = bordereauFile;
				generatorList.add(generator);
				break;
			}
		}
	}

	/**
	 * Charge le fichier de configuration des tests
	 *
	 * @param configFile
	 * @return
	 * @throws TechnicalException
	 */
	public String loadFile(String configFile) throws TechnicalException {
		String retourMsg = "";
		Pattern sectionRegex = null;
		Pattern fileRegex = null;
		Matcher m;
		String line;
		String authorizedFiles;
		String errMsg;
		String g;

		TRACESWRITER.info(INFO_CONFIG_LOAD + configFile);

		sectionRegex = Pattern.compile(SECTION_REGEX);
		try {
			File f = new File(configFile);
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, ENCODAGE);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					line = removeUTF8BOM(line);
					line = line.trim();
					if (StringUtils.isNotEmpty(line) && !line.startsWith(BEGINNING_CAR)) {
						m = sectionRegex.matcher(line);
						if (m.matches()) {
							if (inSection) {
								doSection();
							}
							section = m.group(1);
							authorizedFiles = StringUtils.EMPTY;
							inSection = true;
							if (SECTION_GENERATOR.equals(section)) {
								authorizedFiles = SECTION_GENERATOR_AUTHORIZED_FILES;
							} else {
								errMsg = new StringBuilder().append(ERROR_CONFIG_SECTION_1).append(section)
										.append(ERROR_CONFIG_SECTION_2).toString();
								TRACESWRITER.error(errMsg);
								errorsList.add(errMsg);
								inSection = false;
							}
							if (inSection) {
								sectionName = m.group(3);
								traceFile = StringUtils.EMPTY;
								profileFile = StringUtils.EMPTY;
								dataFile = StringUtils.EMPTY;
								bordereauFile = StringUtils.EMPTY;
								repDocuments = StringUtils.EMPTY;
								baseURI = StringUtils.EMPTY;
								accordVersement = StringUtils.EMPTY;
								fileRegex = Pattern.compile(new StringBuilder().append(AUTHORIZED_FILES_REGEX_BEGIN)
										.append(authorizedFiles).append(AUTHORIZED_FILES_REGEX_END).toString());
							}
						} else {
							if (inSection) {
								m = fileRegex.matcher(line);
								if (m.matches()) {
									g = m.group(1);
									switch (g.toString()) {
									case SUBSECTION_TRACE:
										traceFile = m.group(2);
										break;
									case SUBSECTION_ACCORD:
										accordVersement = m.group(2);
										break;
									case SUBSECTION_REP_DOCUMENTS:
										repDocuments = m.group(2);
										break;
									case SUBSECTION_BASE_URI:
										baseURI = m.group(2);
										break;
									case SUBSECTION_DATA:
										dataFile = m.group(2);
										break;
									case SUBSECTION_BORDEREAU:
										bordereauFile = m.group(2);
										break;
									}
								}
							}
						}
					}
				}
			}
			if (inSection) {
				doSection();
			}
			br.close();
		} catch (FileNotFoundException e) {
			throw new TechnicalException(ERROR_NO_FILE_1 + configFile + ERROR_NO_FILE_2 + e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new TechnicalException(ERROR_READING + configFile, e);
		}
		return retourMsg;
	}

	private static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM)) {
			s = s.substring(1);
		}
		return s;
	}
}
