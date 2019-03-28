package com.wci.umls.server.mojo.analysis.matching;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.FieldedStringTokenizer;

public class SctICD11SynonymProvider {
  public static final Map<String, Set<String>> snomedToIcdSynonymMap = new HashMap<>();

  public static final Map<String, Set<String>> icdToSnomedSynonymMap = new HashMap<>();

  static final protected String synonymsInputFilePath = "src//main//resources//synonyms.txt";

  private Map<String, Set<String>> mapToUse;
  static {

    try {
      BufferedReader reader = new BufferedReader(new FileReader(synonymsInputFilePath));
      String line = reader.readLine(); // Don't want header
      line = reader.readLine();
      while (line != null) {
        if (!line.trim().isEmpty()) {
          String[] columns = line.trim().split("\\|");

          String icdStr = columns[ICD11MatcherConstants.ICD_COLUMN].trim();
          String snomedStr = columns[ICD11MatcherConstants.SNOMED_COLUMN].trim();

          if (icdStr.startsWith("#")) {
            icdStr = icdStr.substring(1);
          }

          if (!snomedToIcdSynonymMap.containsKey(snomedStr)) {
            snomedToIcdSynonymMap.put(snomedStr, new HashSet<String>());
          }
          if (!icdToSnomedSynonymMap.containsKey(icdStr)) {
            icdToSnomedSynonymMap.put(icdStr, new HashSet<String>());
          }
          snomedToIcdSynonymMap.get(snomedStr).add(icdStr);
          icdToSnomedSynonymMap.get(icdStr).add(snomedStr);
        }
        
        line = reader.readLine();
      }

      reader.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public SctICD11SynonymProvider(int direction) {
    if (direction == ICD11MatcherConstants.SNOMED_TO_ICD11) {
      mapToUse = snomedToIcdSynonymMap;
    } else {
      mapToUse = icdToSnomedSynonymMap;
    }
  }

  public Map<String, Set<String>> getMap() throws Exception {
    if (mapToUse == null) {
      throw new Exception("Must define map to use prior to accessing it");
    }
    return mapToUse;
  }

  public Set<String> identifyEquivalencies(String desc) {
    Map<String, Set<String>> synonymMap = identifySynonymMap(desc);

    String[] icd11Tokens = FieldedStringTokenizer.split(desc.trim().toLowerCase(),
        " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

    return identifySynonyms(icd11Tokens, 0, synonymMap, new HashSet<String>());
  }

  private Set<String> identifySynonyms(String[] icd11Tokens, int idx,
    Map<String, Set<String>> synonymMap, Set<String> icd11Equivalencies) {
    if (idx >= icd11Tokens.length) {
      return icd11Equivalencies;
    } else {
      if (idx == 0) {
        icd11Equivalencies.add(icd11Tokens[idx]);

        if (synonymMap.containsKey(icd11Tokens[idx])) {
          for (String s : synonymMap.get(icd11Tokens[idx])) {
            icd11Equivalencies.add(s);
          }
        }

        return identifySynonyms(icd11Tokens, idx + 1, synonymMap, icd11Equivalencies);
      } else {
        Set<String> newEquivalencies = new HashSet<>();

        for (String existingEquivalency : icd11Equivalencies) {
          newEquivalencies.add(existingEquivalency + " " + icd11Tokens[idx]);

          if (synonymMap.containsKey(icd11Tokens[idx])) {
            for (String s : synonymMap.get(icd11Tokens[idx])) {
              newEquivalencies.add(existingEquivalency + " " + s);
            }
          }
        }

        return identifySynonyms(icd11Tokens, idx + 1, synonymMap, newEquivalencies);
      }
    }
  }

  private Map<String, Set<String>> identifySynonymMap(String desc) {
    Map<String, Set<String>> synonymMap = new HashMap<>();

    String[] icd11Tokens = FieldedStringTokenizer.split(desc.trim().toLowerCase(),
        " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

    for (int i = 0; i < icd11Tokens.length; i++) {
      for (String synonymKey : mapToUse.keySet()) {
        if (icd11Tokens[i].equals(synonymKey.toLowerCase().trim())) {
          if (!synonymMap.containsKey(icd11Tokens[i])) {
            synonymMap.put(icd11Tokens[i], new HashSet<String>());
          }

          synonymMap.get(icd11Tokens[i]).addAll(mapToUse.get(synonymKey));
        }
      }
    }

    return synonymMap;
  }
}
