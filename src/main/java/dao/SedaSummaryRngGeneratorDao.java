package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import metier.Sae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sedaProfileGenerator.SAEColumn;
import dao.interfaces.SedaSummaryRngGeneratorDaoInterface;
import exception.TechnicalException;

/**
 * SedaSummaryRngGeneratorDao.java
 */
public class SedaSummaryRngGeneratorDao extends AbstractDao implements SedaSummaryRngGeneratorDaoInterface {

	private static final Logger TRACESWRITER = LoggerFactory.getLogger(SedaSummaryRngGeneratorDao.class);

	private static final String SELECT_PROFILE1 = "SELECT " + SAEColumn.TRANSFERIDPREFIX + ", "
			+ SAEColumn.TRANSFERIDVALUE + ", " + SAEColumn.SAE_PROFILARCHIVAGE + ", " + SAEColumn.TRANSFERRINGAGENCYID
			+ ", " + SAEColumn.TRANSFERRINGAGENCYNAME + ", " + SAEColumn.TRANSFERRINGAGENCYDESC + ", "
			+ SAEColumn.ARCHIVALAGENCYID + ", " + SAEColumn.ARCHIVALAGENCYNAME + ", " + SAEColumn.ARCHIVALAGENCYDESC
			+ " FROM SAE WHERE " + SAEColumn.SAE_ACCORDVERSEMENT + "='";

	private static final String QUERY_PROFILE2 = "' and " + SAEColumn.SAE_SERVEUR + " ='";

	private static final String QUERY_PROFILE3 = "'";

	private static final String UPDATE_PROFILE1 = "UPDATE SAE set " + SAEColumn.TRANSFERIDVALUE + " = ? WHERE "
			+ SAEColumn.SAE_ACCORDVERSEMENT + " ='";

	private static final String ERROR_NO_AGREEMENT_BEGIN = "Impossible de trouver l'accord de versement '";
	private static final String ERROR_NO_AGREEMENT_END = "' dans la table SAE";
	private static final String ERROR_NO_SAE_ENTRY = "Erreur à la tentative de récupération des informations de la table SAE.";
	private static final String ERROR_STATEMENT_CLOSE = "Erreur à la fermeture des objets de récupération d'informations de la base de données.";

	private String url;
	private String username;
	private String password;

	public SedaSummaryRngGeneratorDao(String url, String username, String password) {

		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public Sae getSae(String baseURI, String accordVersement) throws TechnicalException {

		Sae sae = null;
		Statement state = null;
		Statement state2 = null;
		ResultSet result = null;

		try {

			state = getConnection().createStatement();
			result = state.executeQuery(SELECT_PROFILE1 + accordVersement + QUERY_PROFILE2 + baseURI + QUERY_PROFILE3);

			if (result.next()) {

				sae = new Sae();

				sae.setId(Integer.parseInt(result.getString(SAEColumn.TRANSFERIDVALUE.toString())) + 1);
				sae.setTransferId(result.getString(SAEColumn.TRANSFERIDPREFIX.toString())
						+ String.format("%010d", sae.getId()));
				sae.setProfileFile(result.getString(SAEColumn.SAE_PROFILARCHIVAGE.toString()));
				sae.setTransferringAgencyId(result.getString(SAEColumn.TRANSFERRINGAGENCYID.toString()));
				sae.setTransferringAgencyName(result.getString(SAEColumn.TRANSFERRINGAGENCYNAME.toString()));
				sae.setTransferringAgencyDesc(result.getString(SAEColumn.TRANSFERRINGAGENCYDESC.toString()));
				sae.setArchivalAgencyId(result.getString(SAEColumn.ARCHIVALAGENCYID.toString()));
				sae.setArchivalAgencyName(result.getString(SAEColumn.ARCHIVALAGENCYNAME.toString()));
				sae.setArchivalAgencyDesc(result.getString(SAEColumn.ARCHIVALAGENCYDESC.toString()));

				TRACESWRITER.trace(result.getString(SAEColumn.TRANSFERIDPREFIX.toString()) + ", "
						+ result.getString(SAEColumn.TRANSFERIDVALUE.toString()) + ", "
						+ result.getString(SAEColumn.SAE_PROFILARCHIVAGE.toString()) + ", "
						+ result.getString(SAEColumn.TRANSFERRINGAGENCYID.toString()));

			} else {
				throw new TechnicalException(ERROR_NO_AGREEMENT_BEGIN + accordVersement + ERROR_NO_AGREEMENT_END);
			}

			state2 = getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			PreparedStatement prepare = getConnection().prepareStatement(
					UPDATE_PROFILE1 + accordVersement + QUERY_PROFILE2 + baseURI + QUERY_PROFILE3);
			prepare.setString(1, String.valueOf(sae.getId()));
			prepare.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException(ERROR_NO_SAE_ENTRY, e);

		} finally {

			try {
				if (state2 != null) {
					state2.close();
				}

				if (result != null) {
					result.close();
				}

				if (state != null) {
					state.close();
				}

				closeConnection();

			} catch (SQLException e) {
				throw new TechnicalException(ERROR_STATEMENT_CLOSE, e);
			}

		}

		return sae;
	}

	@Override
	String getUrl() {
		return url;
	}

	@Override
	String getUsername() {
		return username;
	}

	@Override
	String getPassword() {
		return password;
	}
}
