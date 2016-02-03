package commonClasses;

public class GeneratorConfig extends Job {
	protected String accordVersement = "";
	protected String bordereauFile = "";
	protected String dataFile = "";
	protected String repDocuments = "";
	protected String baseURI = "";

	public String getAccordVersement() {
		return accordVersement;
	}

	public String getBordereauFile() {
		return bordereauFile;
	}

	public String getDataFile() {
		return dataFile;
	}

	public String getRepDocuments() {
		return repDocuments;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public String getNomJob() {
		return super.nomJob;
	}

	public String getTraceFile() {
		return super.traceFile;
	}
}
