package unit;

public enum SedaGeneratorPropertiesEnum {
	REPOSITORY, DATABASE_URL, DATABASE_USER, DATABASE_PASSWD;
	public static boolean contains(String test) {
		boolean containsLocal = false;
		for (SedaGeneratorPropertiesEnum c : SedaGeneratorPropertiesEnum.values()) {
			if (c.name().equals(test)) {
				containsLocal = true;
			}
		}
		return containsLocal;
	}
}
