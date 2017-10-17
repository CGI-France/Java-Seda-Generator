package dao;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import metier.Sae;
import sedaProfileGenerator.Checker;
import sedaProfileGenerator.FileHashSum;
import sedaProfileGenerator.HashAlgorithm;
import sedaProfileGenerator.SedaSummaryRngGenerator;

import commonClasses.AccordVersementConfig;
import commonClasses.SimpleConfig;

import dao.interfaces.SedaSummaryRngGeneratorDaoInterface;
import exception.TechnicalException;

public class ConfigFileDao implements SedaSummaryRngGeneratorDaoInterface {

	// ERRORS
	private static final String ERROR_CONFIG_NULL = "La configuration est nulle.";
	private static final String ERROR_ACCORD_NOT_FOUND_1 = "Impossible de trouver l'accord de versement '";
	private static final String ERROR_ACCORD_NOT_FOUND_2 = "' pour le serveur '";
	private static final String ERROR_ACCORD_NOT_FOUND_3 = "' dans la configuration";

	private static final String FORMAT_DATE_ID_TRANSFER = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";
	private static final String FORMAT_SHORT_DATE_ID_TRANSFER = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String SEPARATOR_TRANFER_ID = "@";

	private SimpleConfig simpleConfig;
	private File dataFile;
	private SedaSummaryRngGenerator ssg;

	public ConfigFileDao(SimpleConfig simpleConfig, String dataFilePath, SedaSummaryRngGenerator ssg)
			throws TechnicalException {
		if (simpleConfig != null) {
			this.simpleConfig = simpleConfig;
		} else {
			throw new TechnicalException(ERROR_CONFIG_NULL);
		}

		Checker.checkFile(dataFilePath);
		dataFile = new File(dataFilePath);

		this.ssg = ssg;
	}

	@Override
	public Sae getSae(String baseURI, String accordVersement) throws TechnicalException {
		Sae sae = new Sae();

		AccordVersementConfig accordVersementConfig = this.simpleConfig.getAccordVersementConfig(accordVersement,
				baseURI);

		if (accordVersementConfig != null) {

			// Préparation de certaines parties des attributs de l'objet Sae à retourner
			// Calcul du sha-256 du fichier de données métier
			String dataSha256 = FileHashSum.getHash(dataFile, ssg, HashAlgorithm.SHA256);

			SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_ID_TRANSFER);
			String dateString = sdf.format(new Date());

			// Préparation de l'objet Sae à retourner
			StringBuilder transferIdBuilder = new StringBuilder();
			transferIdBuilder.append(accordVersementConfig.getTransferIdPrefix()).append(dataSha256)
					.append(SEPARATOR_TRANFER_ID).append(dateString);
			sae.setTransferId(transferIdBuilder.toString());

			sdf = new SimpleDateFormat(FORMAT_SHORT_DATE_ID_TRANSFER);
			dateString = sdf.format(new Date());
			
			transferIdBuilder.setLength(0);
			transferIdBuilder.append(accordVersementConfig.getTransferIdPrefix()).append(dateString);
			sae.setShortTransferId(transferIdBuilder.toString());
			
			sae.setProfileFile(accordVersementConfig.getSaeProfilArchivage());
			sae.setTransferringAgencyId(accordVersementConfig.getTransferringAgencyId());
			sae.setTransferringAgencyName(accordVersementConfig.getTransferringAgencyName());
			sae.setTransferringAgencyDesc(accordVersementConfig.getTransferringAgencyDesc());
			sae.setArchivalAgencyId(accordVersementConfig.getArchivalAgencyId());
			sae.setArchivalAgencyName(accordVersementConfig.getArchivalAgencyName());
			sae.setArchivalAgencyDesc(accordVersementConfig.getArchivalAgencyDesc());

		} else {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append(ERROR_ACCORD_NOT_FOUND_1)
				.append(accordVersement)
				.append(ERROR_ACCORD_NOT_FOUND_2)
				.append(baseURI)
				.append(ERROR_ACCORD_NOT_FOUND_3);
			throw new TechnicalException(errorMessage.toString());
		}

		return sae; // TODO Raccord de méthode auto-généré
	}

}
