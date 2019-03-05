package com.wci.umls.server.mojo.processes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.mojo.model.SctSourceDescription;

public class SctSourceDescriptionParser {
	private final List<String> NEOPLASM_SYNONYMS = Arrays.asList("neoplasm", "neoplasms", "neoplastic", "tumor",
			"tumorous", "tumoru", "tumour", "tumoural", "tumours", "cancer", "cancerous", "cancerphobia", "carcinoma",
			"carcinomas", "carcinomatosis", "carcinomatous", "carcinoma-induced", "carcinomaphobia", "Adenocarcinoma",
			"adenoma", "Chondromatosis", "Chromaffinoma", "Glioma", "neoplasia", "Pheochromocytoma",
			"Proliferating pilar cyst", "Thymoma", "Melanoma", "Melanocytic", "Lipoma", "mesothelioma", "sarcoma",
			"fibroma", "papilloma", "Lymphoma", "Chondroma", "squamous");

	private final List<String> FALSE_POSITIVE_BODY_STRUCTURES = Arrays.asList("Borst-Jadassohn", "Brodie", "Brooke",
			"Buschke-Löwenstein", "Buschke-Lowenstein", "Degos", "Enzinger", "Ferguson-Smith", "Gougerot and Carteaud",
			"Ito", "Jadassohn", "Langerhans", "Leser-Trélat", "Malherbe", "Nikolowski", "Ota", "Pinkus", "Queyrat",
			"Reed", "Stewart and Treves", "Vater", "adulthood", "ambiguous lineage", "care", "childhood", "elderly",
			"infancy", "xiphoid process");

	private final List<String> UNCERTAINTY = Arrays.asList("undetermined significance", "unknown origin",
			"unknown origin or ill-defined site", "unknown primary", "uncertain behavior", "uncertain behaviour",
			"uncertain or unknown behavior", "uncertain or unknown behaviour");

	private Map<String, String> apostropheMap = new HashMap<>();

	private int counter = 0;

	private final List<String> BODY_STRUCTURE_SPLIT = Arrays.asList(" of ", " in ", " from ", ", with ", "with ",
			" due to ", " - ", "-", ", ");

	private final List<String> BODY_STRUCTURE_DASH_SPLIT_EXCEPTIONS = Arrays.asList("-cell", "pharyngo-", "mucosa-",
			"cardio-", "gastro-", "ill-", "intra-", "lower-", "non-", "two-", "upper-", "co-", "para-");

	private Set<String> distinctBodyStructures = new HashSet<>();

	private List<String> bodyStructuresRequireSecondaryInfo = Arrays.asList("arterial cartilage", "blood vessel",
			"bone ", "bone and arterial cartilage", "bones", "bone structure", "brain", "connective and soft tissue",
			"connective and soft tissues", "connective tissue", "epithelium", "lymph node", "lymph node from neoplasm",
			"lymph node sites", "lymph nodes", "mucosa", "mucous membrane", "muscle", "non-pigmented epithelium",
			"peripheral nerve", "peripheral nerves", "pigmented epithelium", "ribs", "skin",
			"skin and subcutaneous tissue", "skin and/or subcutaneous tissue", "skin structure", "soft tissue",
			"soft tissues", "spinal cord", "uterus", "vermilion border", "bertebra", "vertevral column", "vestibule");

	/** The output file path. */
	private boolean testing = false;

	/** The output file path for relationships. */
	private final String previousExecutionInputFilePath = "C:\\Users\\yishai\\Desktop\\Neoplasm\\Neoplasm Descriptions v5.txt";

	public SctSourceDescriptionParser() {
		try {
			// Preprocess file to identify unique body structures
			BufferedReader reader = new BufferedReader(new FileReader(previousExecutionInputFilePath));

			String line = reader.readLine(); // Don't want header
			line = reader.readLine();
			while (line != null) {
				String[] columns = line.split("\t");
				if (columns.length > 4 && !columns[4].isEmpty()) {
					distinctBodyStructures.add(columns[4]);
				}
				line = reader.readLine();
			}
			reader.close();
			
			apostropheMap.put("Meckel's diverticulum", "Meckel diverticulum");
			apostropheMap.put("Waldeyer's ring", "Waldeyer ring");
			apostropheMap.put("Bartholin's gland", "Bartholin gland");
			apostropheMap.put("Douglas' pouch", "the pouch of Douglas");
			apostropheMap.put("Gartner's duct", "Gartner duct");
		} catch (Exception e) {
			System.out.println("Failed processing input file: '" + previousExecutionInputFilePath + "' with exception: "
					+ e.getMessage());
		}
	}

	public SctSourceDescription parse(String descString) {

		SctSourceDescription desc = new SctSourceDescription();

		try {
			// desc = "Pathological fracture of hip due to neoplastic disease";

			if (testing) {
				if (counter == 0) {
					descString = "";
				} else if (counter == 1) {
					descString = "Neoplasm of uterus affecting pregnancy";
				} else if (counter == 2) {
					descString = "Neoplasm of uncertain behaviour of salivary gland duct";
					/*
					 * } else if (counter == 3) { desc =
					 * "Mixed cell type lymphosarcoma of lymph nodes of head";
					 * 
					 * } else if (counter == 4) { desc =
					 * "Hodgkin's paragranuloma of intrathoracic lymph nodes";
					 */
				} else {
					descString = "";
				}

				if (counter > 4 || descString.isEmpty()) {
					return null;
				}
			}

			desc.setDescription(descString.trim());

			// Pathology Representation
			for (String syn : NEOPLASM_SYNONYMS) {
				if (descString.toLowerCase().contains(syn.toLowerCase())) {
					desc.setNeoplasmSynonym(syn.trim());
					break;
				}
			}

			boolean containsBodyStructure = true;
			String secondaryInfo = null;

			String splitKeyWord = identifySplitKeyword(descString);
			// of body structure
			if (splitKeyWord != null && descString.contains(splitKeyWord)) {
				int bodyStructIdx = descString.indexOf(splitKeyWord);

				if (splitKeyWord.equals(" of ")) {
					// Ignore Local Recurrence 'of'
					if (descString.startsWith("Local recurrence of")) {
						bodyStructIdx = descString.substring("Local recurrence of".length()).indexOf(" of ")
								+ "Local recurrence of".length();
					}

					// Ignore 'of' overlapping lesion
					if (descString.substring(bodyStructIdx).toLowerCase().contains("of overlapping lesion")) {
						bodyStructIdx = bodyStructIdx
								+ descString.substring(bodyStructIdx + "of overlapping lesion".length()).indexOf(" of ")
								+ "of overlapping lesion".length();
					}
				}

				// Ignore 'of' uncertain xyz
				for (String uncertainStr : UNCERTAINTY) {
					String afterSplitWord = descString.substring(bodyStructIdx + splitKeyWord.length()).trim()
							.toLowerCase();
					if (afterSplitWord.startsWith(uncertainStr)) {
						String secondSplitKeyWord = identifySplitKeyword(afterSplitWord);

						if (secondSplitKeyWord != null) {
							bodyStructIdx = descString.indexOf(splitKeyWord) + splitKeyWord.length();
							bodyStructIdx += afterSplitWord.indexOf(secondSplitKeyWord);
						} else {
							containsBodyStructure = false;
						}

						/*
						 * if (!afterSplitWord.endsWith(uncertainStr)) { secondaryIdx =
						 * afterSplitWord.indexOf(splitKeyWord.trim()) + bodyStructIdx +
						 * splitKeyWord.length(); secondaryInfo = desc.substring(secondaryIdx).trim(); }
						 */ break;
					}
				}

				if (FALSE_POSITIVE_BODY_STRUCTURES
						.contains(descString.substring(descString.indexOf("of") + "of".length()).trim())) {
					containsBodyStructure = false;
				}

				// Print content prior to Body Structure
				if (!containsBodyStructure) {
					if (secondaryInfo == null) {
						desc.setPathology(descString.trim());
					}
				} else {
					// Body Structure found... Print part prior to Body Structure

					// Print Pathology
					if (descString.substring(0, bodyStructIdx).trim().endsWith(" " + splitKeyWord.trim())) {
						desc.setPathology(
								descString.substring(0, bodyStructIdx).trim().substring(0, bodyStructIdx - 3).trim());
					} else {
						desc.setPathology(descString.substring(0, bodyStructIdx).trim());
					}

					// Identify Body Structure and if available, causation and/or secondary
					// Pathology
					String bodyStruct = descString.substring(bodyStructIdx + 4);
					String originalBodyStruture = bodyStruct;

					if (bodyStruct.contains("(") && bodyStruct.trim().endsWith(")") && !bodyStruct.endsWith("(s)")) {
						// If ends with paranthesis (unless plural), make part in parenthesis as
						// secondary Info
						secondaryInfo = bodyStruct.substring(bodyStruct.indexOf("(")).trim();
						secondaryInfo = secondaryInfo.substring(1, secondaryInfo.length() - 1);
						bodyStruct = bodyStruct.substring(0, bodyStruct.indexOf("(")).trim();
					} else if (bodyStruct.contains("affecting")) {
						// If contains "affecting", make everything afterwards (including the word) as
						// secondary Info
						secondaryInfo = bodyStruct.substring(bodyStruct.indexOf("affecting")).trim();
						bodyStruct = bodyStruct.substring(0, bodyStruct.indexOf("affecting")).trim();
					} else {
						for (String splitStr : BODY_STRUCTURE_SPLIT) {
							if (bodyStruct.contains(splitStr)) {
								boolean isException = false;

								if (splitStr.trim().equals("-")) { // && !desc.matches(".* [(].*-.*") --> Removing this
																	// fixed skin (T-cell, skin (chronic T-cell and
																	// others

									for (String exception : BODY_STRUCTURE_DASH_SPLIT_EXCEPTIONS) {
										if (bodyStruct.contains(exception)) {
											isException = true;
											break;
										}
									}
								}

								if (!isException) {
									secondaryInfo = bodyStruct
											.substring(bodyStruct.indexOf(splitStr) + splitStr.length()).trim();

									if (secondaryInfo.endsWith(")")) {
										secondaryInfo = secondaryInfo.substring(0, secondaryInfo.lastIndexOf(")"));
									}
									bodyStruct = bodyStruct.substring(0, bodyStruct.indexOf(splitStr)).trim();
								}
								break;
							}
						}
					}

					if (bodyStruct.contains("'") && apostropheMap.containsKey(bodyStruct)) {
						bodyStruct = apostropheMap.get(bodyStruct);
					}

					// Check for Lymph Node special case
					if (bodyStruct.contains("lymph node") && !bodyStruct.trim().startsWith("lymph node")) {
						if (secondaryInfo == null || secondaryInfo.isEmpty()) {
							secondaryInfo = bodyStruct.substring(0, bodyStruct.indexOf("lymph node")).trim();
							bodyStruct = bodyStruct.substring(bodyStruct.indexOf("lymph node")).trim();
						}
					}

					// Check to see if
					if (!bodyStructuresRequireSecondaryInfo.contains(bodyStruct) && secondaryInfo != null) {
						for (String structure : distinctBodyStructures) {
							if (secondaryInfo.contains(structure)) {
								bodyStruct = originalBodyStruture;
								secondaryInfo = null;
								break;
							}
						}
					}

					// Print out body structure
					if (!FALSE_POSITIVE_BODY_STRUCTURES.contains(bodyStruct)) {
						if (bodyStruct.startsWith("the ")) {
							bodyStruct = bodyStruct.substring("the ".length()).trim();
						}
						desc.setBodyStructure(bodyStruct.trim());
					}

					// Print Secondary Information
					if (secondaryInfo != null) {
						desc.setSecondInfo(secondaryInfo.trim());
					}
				}
			}

			outputBooleanValues(descString, desc);

			return desc;

		} catch (Exception e) {
			System.out.println("Failed processing: '" + descString + "' with exception: " + e.getMessage());
			return null;
		}
	}

	private String identifySplitKeyword(String desc) {
		if (!desc.contains("due to")) {
			if (!desc.contains(" to ") && desc.contains(" of ")) {
				// i.e. Neoplasm of hypogastric lymph nodes
				return " of ";
			} else if (desc.contains(" to ") && !desc.contains(" of ")) {
				// i.e. Cancer metastatic to choroid
				return " to ";
			} else if (!desc.matches(".* to .* to .*") && !desc.matches(".* of .* of .*")
					&& desc.matches(".* to .* of .*")) {
				// i.e. Cancer metastatic to lymph nodes of lower limb
				return " to ";
			}
		} else {
			if (!desc.matches(".* to .* to .*") && desc.matches(".* of .* due to .*")) {
				// i.e. Pathological fracture of hip due to neoplastic disease
				return " of ";
			} else if (!desc.contains("of") && !desc.matches(".* to .* to .*")) {
				// i.e. Pelvis fracture due to tumor
				return null;
			}
		}

		return null;
	}

	private void outputBooleanValues(String descString, SctSourceDescription desc) {
		// Has Uncertainty
		for (String uncertainStr : UNCERTAINTY) {
			if (descString.toLowerCase().contains(uncertainStr)) {
				desc.setUncertainty(uncertainStr.trim());
				break;
			}
		}

		// Has 'Cancer Stage'
		if (descString.toLowerCase().contains("stage")) {
			desc.setStage(true);
		}

		// Is primary or secondary
		if (descString.toLowerCase().contains("primary")) {
			desc.setPrimaryOrSecondary("Primary");
		} else if (descString.toLowerCase().contains("secondary")) {
			desc.setPrimaryOrSecondary("Secondary");
		}

		// Is Benign or Malignant
		if (descString.toLowerCase().contains("benign")) {
			desc.setBenignOrMalignant("Benign");
		} else if (descString.toLowerCase().contains("malignant")) {
			desc.setBenignOrMalignant("Malignant");
		}

		// Is Upper or Lower
		if (descString.toLowerCase().contains("upper")) {
			desc.setUpperOrLower("Upper");
		} else if (descString.toLowerCase().contains("lower")) {
			desc.setUpperOrLower("Lower");
		}

		// Is Right or Left
		if (descString.toLowerCase().contains("right")) {
			desc.setLeftOrRight("Right");
		} else if (descString.toLowerCase().contains("left")) {
			desc.setLeftOrRight("Left");
		}

		// Is Metastic
		if (descString.toLowerCase().contains("metastatic")) {
			desc.setMetastatic(true);
		}

		// Has in situ
		if (descString.toLowerCase().contains("in situ")) {
			desc.setInSitu(true);
		}

		// Has Node
		if (descString.toLowerCase().contains("node")) {
			desc.setNode(true);
		}

		// Is Local recurrence
		if (descString.toLowerCase().contains("local recurrence")) {
			desc.setLocalRecurrance(true);
		}
	}
}
