package krasa.release.tokenization;

import static org.apache.commons.lang3.StringUtils.substring;

import java.util.*;

import com.google.gson.Gson;

public class Default {

	public static final String SVN_REPO_URL = "http://svn/sdp";
	// public static final String TEMP = "target/branchPrepare-" + System.currentTimeMillis() + "-";
	public static final String TEMP = "target/branchPrepare";

	protected static TokenizationJobParameters loadFromJsonFake() {
		List<ReplacementDefinition> replacementDefinitions = new ArrayList<>();
		addPom(replacementDefinitions);
		addProperties(replacementDefinitions);

		Map<String, String> stringStringHashMap = new TreeMap<>();
		//
		stringStringHashMap.put("old.version", "9999");
		stringStringHashMap.put("new.version", "14100");
		stringStringHashMap.put("old.pom.version", "99.9.9-SNAPSHOT");

		stringStringHashMap.put("new.build.version", "14100");
		stringStringHashMap.put("new.portal.db.version", "14100");
		stringStringHashMap.put("new.sac.db.version", "14100");
		stringStringHashMap.put("new.pit.db.version", "14100");
		stringStringHashMap.put("new.pom.version", "14.1.0");
		TokenizationJobParameters tokenizationJobParameters = new TokenizationJobParameters(replacementDefinitions,
				stringStringHashMap);
		String s1 = tokenizationJobParameters.toJson(tokenizationJobParameters);

		return loadFromJson(s1);
	}

	public static String generateJson(String oldVersion, String newVersion, String newPortalDb, String newSacDb,
			String newPitDb) {
		List<ReplacementDefinition> replacementDefinitions = new ArrayList<>();
		addPom(replacementDefinitions);
		addProperties(replacementDefinitions);

		Map<String, String> stringStringHashMap = new TreeMap<>();
		//
		stringStringHashMap.put("old.version", oldVersion);
		stringStringHashMap.put("new.version", newVersion);
		stringStringHashMap.put("old.pom.version", toPomVersion(oldVersion));

		stringStringHashMap.put("new.build.version", newVersion);
		stringStringHashMap.put("new.portal.db.version", newPortalDb);
		stringStringHashMap.put("new.sac.db.version", newSacDb);
		stringStringHashMap.put("new.pit.db.version", newPitDb);
		stringStringHashMap.put("new.pom.version", toPomVersion(newVersion));
		TokenizationJobParameters tokenizationJobParameters = new TokenizationJobParameters(replacementDefinitions,
				stringStringHashMap);
		String s1 = tokenizationJobParameters.toJson(tokenizationJobParameters);

		return s1;
	}

	protected static String toPomVersion(String fromVersion) {
		StringBuilder sb = new StringBuilder();
		sb.append(substring(fromVersion, 0, 2));
		sb.append(".");
		sb.append(substring(fromVersion, 2, 3));
		sb.append(".");
		sb.append(substring(fromVersion, 3, 4));

		String substring = substring(fromVersion, 4, 5);
		if (substring.length() > 0 && !substring.equals("0")) {
			sb.append(".");
			sb.append(substring);
		}

		if ("9999".equals(fromVersion)) {
			sb.append("-SNAPSHOT");
		}
		return sb.toString();
	}

	protected static TokenizationJobParameters loadFromJson(String s1) {
		return new Gson().fromJson(s1, TokenizationJobParameters.class);
	}

	private static void addProperties(List<ReplacementDefinition> replacementDefinitions) {
		ReplacementDefinition definition = new ReplacementDefinition();
		replacementDefinitions.add(definition);

		final List<String> includes = definition.getIncludes();
		includes.add("**/*.properties");
		/* spi-pai */
		includes.add("**/*.sql");
		includes.add("**/PartnerContractDataProviderTest.java");

		final List<Replacement> replacements = definition.getReplacements();
		replacements.add(new Replacement("build.number=${old.version}", "build.number=" + "${new.build.version}"));
		replacements.add(new Replacement("pit${old.version}", "pit" + "${new.pit.db.version}"));
		replacements.add(new Replacement("pai${old.version}", "pai" + "${new.portal.db.version}"));
		replacements.add(new Replacement("sac${old.version}", "sac" + "${new.sac.db.version}"));
		replacements.add(new Replacement("sdf${old.version}", "sdf" + "${new.portal.db.version}"));
		replacements.add(new Replacement(".version=${old.version}", ".version=" + "${new.version}"));
		replacements.add(new Replacement("default.component_id=${old.version}", "default.component_id="
				+ "${new.portal.db.version}"));
		replacements.add(new Replacement("default.docrootapp_id=${old.version}", "default.docrootapp_id="
				+ "${new.portal.db.version}"));
	}

	private static void addPom(List<ReplacementDefinition> replacementDefinitions) {
		ReplacementDefinition src = new ReplacementDefinition();
		replacementDefinitions.add(src);
		src.getIncludes().add("**/pom.xml");
		src.getReplacements().add(new Replacement("${old.pom.version}", "${new.pom.version}"));
	}

}