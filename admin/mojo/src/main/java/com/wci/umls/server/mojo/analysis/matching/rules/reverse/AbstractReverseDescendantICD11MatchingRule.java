package com.wci.umls.server.mojo.analysis.matching.rules.reverse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.ICD11MatcherConstants;
import com.wci.umls.server.mojo.analysis.matching.SctICD11SynonymProvider;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.SctNeoplasmDescriptionParser;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;
import com.wci.umls.server.rest.client.ContentClientRest;

public abstract class AbstractReverseDescendantICD11MatchingRule extends AbstractICD11MatchingRule {
  protected static SctICD11SynonymProvider synonymProvider =
      new SctICD11SynonymProvider(ICD11MatcherConstants.ICD11_TO_SNOMED);

  public AbstractReverseDescendantICD11MatchingRule(ContentClientRest contentClient, String st,
      String sv, String tt, String tv, String token) {
    super(contentClient, st, sv, tt, tv, token);
  }

  protected SctICD11SynonymProvider getSynonymProvider() {
    return synonymProvider;
  }

  /**
   * Construct con info str.
   *
   * @param findingSites the finding sites
   * @param sctCon the sct con
   * @param counter the counter
   * @return the string buffer
   */
  protected StringBuffer createSnomedConceptSearchedLine(ICD11MatcherSctConcept sctCon,
    int counter) {

    StringBuffer newConInfoStr = new StringBuffer();
    newConInfoStr.append("\n#" + counter + " Snomed Concept: " + sctCon.getName() + "\tSctId: "
        + sctCon.getConceptId() + "\twith Ancestor: ");

    return newConInfoStr;
  }

  public ICD11MatcherSctConcept identifyEquivalentSctCon(SearchResult icd11Con) throws Exception {
    Set<String> icd11Equivalencies = synonymProvider.identifyEquivalencies(icd11Con.getValue());

    for (String icd11Syn : icd11Equivalencies) {
      SearchResultList results =
          client.findConcepts(sourceTerminology, sourceVersion, icd11Syn, pfsLimited, authToken);

      for (SearchResult sctResult : results.getObjects()) {
        if (!sctResult.isObsolete()) {
          Concept fullSctCon = client.getConcept(sctResult.getTerminologyId(), sourceTerminology,
              sourceVersion, null, authToken);

          for (Atom atom : fullSctCon.getAtoms()) {
            if (conceptSearcher.isValidDescription(atom)
                && isEquivalent(atom.getName(), icd11Syn)) {
              return conceptSearcher.populateSctConcept(sctResult.getTerminologyId(),
                  fullSctCon.getAtoms(), null);
            }
          }
        }
      }
    }

    return null;
  }

  // Considered equivalent if outside of non-important strings
  // a) all strings in sct is in icd11
  // b) all strings in icd11 is in sct
  private boolean isEquivalent(String sct, String icd11) {
    String[] icd11Tokens = FieldedStringTokenizer.split(icd11.trim().toLowerCase(),
        " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

    for (int i = 0; i < icd11Tokens.length; i++) {
      if (!ICD11MatcherConstants.NON_MATCHING_TERMS.contains(icd11Tokens[i])
          && !sct.toLowerCase().matches(".*\\b" + icd11Tokens[i] + "\\b.*")) {
        return false;
      }
    }

    String[] sctTokens = FieldedStringTokenizer.split(sct.trim().toLowerCase(),
        " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

    for (int i = 0; i < sctTokens.length; i++) {
      if (!ICD11MatcherConstants.NON_MATCHING_TERMS.contains(sctTokens[i])
          && !icd11.toLowerCase().matches(".*\\b" + sctTokens[i] + "\\b.*")) {
        return false;
      }
    }

    return true;
  }

  @Override
  protected boolean printIcd11Targets() {
    return true;
  }

  @Override
  public String getRuleName() {
    return getRuleId();
  }

  @Override
  public String getDefaultTarget() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, ICD11MatcherSctConcept> getConceptMap() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getEclExpression() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDefaultSkinMatch() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected ICD11MatcherSctConcept getTopLevelConcept() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> executeRule(ICD11MatcherSctConcept sctCon, int counter) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void preTermProcessing(ICD11MatcherSctConcept sctCon) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Set<String> getRuleBasedNonMatchTerms() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean executeContentParsers(String matcherName, SctNeoplasmDescriptionParser descParser,
    SctRelationshipParser relParser) throws IOException {
    return false;
  }
}