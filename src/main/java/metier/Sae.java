package metier;

/**
 * Contient un tuple de la base de données, correspondant à un profil.
 */
public class Sae {

	private int id;
	private String transferId;
	private String shortTransferId;
	private String profileFile;
	private String TransferringAgencyId;
	private String TransferringAgencyName;
	private String TransferringAgencyDesc;
	private String ArchivalAgencyId;
	private String ArchivalAgencyName;
	private String ArchivalAgencyDesc;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTransferId() {
		return transferId;
	}

	public void setTransferId(String transferId) {
		this.transferId = transferId;
	}

	public String getProfileFile() {
		return profileFile;
	}

	public void setProfileFile(String profileFile) {
		this.profileFile = profileFile;
	}

	public String getTransferringAgencyId() {
		return TransferringAgencyId;
	}

	public void setTransferringAgencyId(String transferringAgencyId) {
		TransferringAgencyId = transferringAgencyId;
	}

	public String getTransferringAgencyName() {
		return TransferringAgencyName;
	}

	public void setTransferringAgencyName(String transferringAgencyName) {
		TransferringAgencyName = transferringAgencyName;
	}

	public String getTransferringAgencyDesc() {
		return TransferringAgencyDesc;
	}

	public void setTransferringAgencyDesc(String transferringAgencyDesc) {
		TransferringAgencyDesc = transferringAgencyDesc;
	}

	public String getArchivalAgencyId() {
		return ArchivalAgencyId;
	}

	public void setArchivalAgencyId(String archivalAgencyId) {
		ArchivalAgencyId = archivalAgencyId;
	}

	public String getArchivalAgencyName() {
		return ArchivalAgencyName;
	}

	public void setArchivalAgencyName(String archivalAgencyName) {
		ArchivalAgencyName = archivalAgencyName;
	}

	public String getArchivalAgencyDesc() {
		return ArchivalAgencyDesc;
	}

	public void setArchivalAgencyDesc(String archivalAgencyDesc) {
		ArchivalAgencyDesc = archivalAgencyDesc;
	}

	public String getShortTransferId() {
		return shortTransferId;
	}

	public void setShortTransferId(String shorttransferId) {
		this.shortTransferId = shorttransferId;
	}

}
