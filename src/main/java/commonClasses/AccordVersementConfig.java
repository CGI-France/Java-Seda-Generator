package commonClasses;

public class AccordVersementConfig {
	@Override
	public String toString() {
		return "AccordVersementConfig [accordVersement=" + accordVersement + ", saeServeur=" + saeServeur
				+ ", transferIdPrefix=" + transferIdPrefix + ", saeProfilArchivage=" + saeProfilArchivage
				+ ", transferringAgencyId=" + transferringAgencyId + ", transferringAgencyName="
				+ transferringAgencyName + ", transferringAgencyDesc=" + transferringAgencyDesc + ", archivalAgencyId="
				+ archivalAgencyId + ", archivalAgencyName=" + archivalAgencyName + ", archivalAgencyDesc="
				+ archivalAgencyDesc + "]";
	}

	private String accordVersement;
	private String saeServeur;
	private String transferIdPrefix;
	private String saeProfilArchivage;
	private String transferringAgencyId;
	private String transferringAgencyName;
	private String transferringAgencyDesc;
	private String archivalAgencyId;
	private String archivalAgencyName;
	private String archivalAgencyDesc;

	public String getAccordVersement() {
		return accordVersement;
	}

	public void setAccordVersement(String accordVersement) {
		this.accordVersement = accordVersement;
	}

	public String getSaeServeur() {
		return saeServeur;
	}

	public void setSaeServeur(String saeServeur) {
		this.saeServeur = saeServeur;
	}

	public String getTransferIdPrefix() {
		return transferIdPrefix;
	}

	public void setTransferIdPrefix(String transferIdPrefix) {
		this.transferIdPrefix = transferIdPrefix;
	}

	public String getSaeProfilArchivage() {
		return saeProfilArchivage;
	}

	public void setSaeProfilArchivage(String saeProfilArchivage) {
		this.saeProfilArchivage = saeProfilArchivage;
	}

	public String getTransferringAgencyId() {
		return transferringAgencyId;
	}

	public void setTransferringAgencyId(String transferringAgencyId) {
		this.transferringAgencyId = transferringAgencyId;
	}

	public String getTransferringAgencyName() {
		return transferringAgencyName;
	}

	public void setTransferringAgencyName(String transferringAgencyName) {
		this.transferringAgencyName = transferringAgencyName;
	}

	public String getTransferringAgencyDesc() {
		return transferringAgencyDesc;
	}

	public void setTransferringAgencyDesc(String transferringAgencyDesc) {
		this.transferringAgencyDesc = transferringAgencyDesc;
	}

	public String getArchivalAgencyId() {
		return archivalAgencyId;
	}

	public void setArchivalAgencyId(String archivalAgencyId) {
		this.archivalAgencyId = archivalAgencyId;
	}

	public String getArchivalAgencyName() {
		return archivalAgencyName;
	}

	public void setArchivalAgencyName(String archivalAgencyName) {
		this.archivalAgencyName = archivalAgencyName;
	}

	public String getArchivalAgencyDesc() {
		return archivalAgencyDesc;
	}

	public void setArchivalAgencyDesc(String archivalAgencyDesc) {
		this.archivalAgencyDesc = archivalAgencyDesc;
	}

}
