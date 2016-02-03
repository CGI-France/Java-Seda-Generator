package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import exception.TechnicalException;

/**
 * AbstractDao.java
 */
public abstract class AbstractDao {

	private static final String ERROR_OPENING_DB_1 = "Impossible d'ouvrir une connexion à la base de données avec les url, username et password fournis";
	private static final String ERROR_CLOSING_DB = "Erreur à la fermeture de la connexion à la base de données.";

	private Connection conn;

	abstract String getUrl();

	abstract String getUsername();

	abstract String getPassword();

	final Connection getConnection() throws TechnicalException {

		if (conn == null) {

			try {

				conn = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
			} catch (SQLException e) {

				throw new TechnicalException(ERROR_OPENING_DB_1, e);
			}
		}

		return conn;
	}

	protected void closeConnection() throws TechnicalException {

		if (conn != null) {
			try {

				conn.close();
			} catch (SQLException e) {

				throw new TechnicalException(ERROR_CLOSING_DB, e);
			}
		}

	}
}
