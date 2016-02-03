package dao.interfaces;

import metier.Sae;
import exception.TechnicalException;

/**
 * SedaSummaryRngGeneratorDao.java
 */
public interface SedaSummaryRngGeneratorDaoInterface {

	Sae getSae(String baseURI, String accordVersement) throws TechnicalException;
}
