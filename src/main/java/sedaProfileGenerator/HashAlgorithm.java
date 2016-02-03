package sedaProfileGenerator;

public enum HashAlgorithm {
	SHA1("SHA-1", "http://www.w3.org/2000/09/xmldsig#sha1"), SHA256("SHA-256",
			"http://www.w3.org/2001/04/xmlenc#sha256");

	private final String hashAlgorithmName;
	private final String hashAlgorithmUrl;

	/**
	 * @param hashAlgorithm
	 */
	private HashAlgorithm(String hashAlgorithmName, String hashAlgorithmUrl) {
		this.hashAlgorithmName = hashAlgorithmName;
		this.hashAlgorithmUrl = hashAlgorithmUrl;
	}

	@Override
	public String toString() {
		return hashAlgorithmUrl;
	}

	public static HashAlgorithm get(String hashGiven) {
		for (HashAlgorithm m : values())
			if (hashGiven.equals(m.toString()))
				return m;
		return null;
	}

	public String getHashAlgorithmUrl() {
		return this.hashAlgorithmUrl;
	}

	public String getHashAlgorithmName() {
		return this.hashAlgorithmName;
	}
}
