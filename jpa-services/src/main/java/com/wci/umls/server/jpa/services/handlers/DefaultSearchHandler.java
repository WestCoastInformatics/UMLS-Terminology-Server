/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;

import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PfscParameter;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.IndexUtility;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Default implementation of {@link SearchHandler}. This provides an algorithm to aide
 * in lucene searches.
 */
public class DefaultSearchHandler implements SearchHandler {

  /** The string field names map. */
  private static Map<Class<?>, Set<String>> stringFieldNames = new HashMap<>();

  /** The field names map. */
  private static Map<Class<?>, Set<String>> allFieldNames = new HashMap<>();

  /** The sort field analyzed map. */
  public static Map<String, Map<String, Boolean>> sortFieldAnalyzedMap =
      new HashMap<>();
  
  static {

    try {

      Class<?>[] classes =
          new Class<?>[] {
              ConceptJpa.class, DescriptorJpa.class, CodeJpa.class,
              ConceptRelationshipJpa.class, ConceptSubsetMemberJpa.class,
              ConceptTreePositionJpa.class
          };

      for (Class<?> clazz : classes) {
        stringFieldNames.put(clazz,
            IndexUtility.getIndexedStringFieldNames(clazz, true));
        allFieldNames.put(clazz,
            IndexUtility.getIndexedStringFieldNames(clazz, false));
      }
    } catch (Exception e) {
      e.printStackTrace();
      stringFieldNames = null;
    }
  }
  
  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <T extends AtomClass> List<T> getLuceneQueryResults(
    String terminology, String version, String branch, String query,
    Class<?> fieldNamesKey, Class<T> clazz, PfsParameter pfs, 
    int[] totalCt, EntityManager manager)
    throws Exception {


    FullTextQuery fullTextQuery =
        applyPfsToLuceneQuery(clazz, fieldNamesKey, query, 
            pfs, manager, terminology, version);

    // Apply paging and sorting parameters - if no search criteria
    if (pfs instanceof PfscParameter
        && ((PfscParameter) pfs).getSearchCriteria().isEmpty()) {
      // Get result size if we know it.
      totalCt[0] = fullTextQuery.getResultSize();
    } else {
      // If with search criteria, save paging
      fullTextQuery.setFirstResult(0);
      fullTextQuery.setMaxResults(Integer.MAX_VALUE);
      totalCt[0] = fullTextQuery.getResultSize();
    }

    // execute the query
    @SuppressWarnings("unchecked")
    List<T> classes = fullTextQuery.getResultList();

    // Use this code to see the actual score values
    // fullTextQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.ID);
    // List<T> classes = new ArrayList<>();
    // List<Object[]> obj = fullTextQuery.getResultList();
    // for (Object[] objArray : obj) {
    // Object score = objArray[0];
    // long id = (Long)objArray[1];
    // T t = getComponent( id, clazz);
    // classes.add(t);
    // Logger.getLogger(getClass()).info(t.getName() + " = " + score);
    // }

    return classes;
    
  }

  /**
   * Apply pfs to lucene query2.
   *
   * @param clazz the clazz
   * @param fieldNamesKey the field names key
   * @param query the query
   * @param pfs the pfs
   * @return the full text query
   * @throws Exception the exception
   */
  protected FullTextQuery applyPfsToLuceneQuery(Class<?> clazz,
    Class<?> fieldNamesKey, String query, PfsParameter pfs, 
    EntityManager manager, String terminology, String version) throws Exception {

    String escapedQuery = query;
    if (query.startsWith("\"") && query.endsWith("\"")) {
      escapedQuery = escapedQuery.substring(1);
      escapedQuery = escapedQuery.substring(0,query.length()-1);
    }  
    // TODO: deal with replaceAll error
    for (String chars : new String[] {"+", "-", "&", "|", "!", "(", ")", "{", "}",
          "[", "]", "^", "~", "*", "?", ":", "\\"} ) {
      escapedQuery = escapedQuery.replaceAll("\\"+ chars,"\\" + chars);
    }
    for (String chars : new String[] {"\""} ) {
    escapedQuery = escapedQuery.replaceAll("\\"+ chars,"\\\\" + chars);
  }
    escapedQuery = "\"" + escapedQuery + "\"";

    
    FullTextQuery fullTextQuery = null;

    // Build up the query
    StringBuilder pfsQuery = new StringBuilder();
    
    pfsQuery.append("(")/*.append(query == null || query.isEmpty() ? "" : query + " OR ")*/;
    pfsQuery.append("atoms.nameSort:" + escapedQuery).append(")");
    
    if (terminology != null && !terminology.equals("") && version != null
        && !version.equals("")) {
      pfsQuery.append(" AND terminology:" + terminology + " AND version:"
          + version);      
    }
    
    if (pfs != null) {
      if (pfs.getActiveOnly()) {
        pfsQuery.append(" AND obsolete:false");
      }
      if (pfs.getInactiveOnly()) {
        pfsQuery.append(" AND obsolete:true");
      }
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfsQuery.append(" AND " + pfs.getQueryRestriction());
      }
    }
    
    Logger.getLogger(getClass()).info(
        "query for " + clazz.getName() + ": " + pfsQuery);

    
    // Set up the "full text query"
    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();

    Query luceneQuery;
    QueryParser queryParser = null;
    try {
      queryParser =
          new MultiFieldQueryParser(stringFieldNames.get(fieldNamesKey)
              .toArray(new String[] {}), searchFactory.getAnalyzer(clazz));
      luceneQuery = queryParser.parse(pfsQuery.toString());

    } catch (ParseException e) {

      try {
        // Code for escaping the query
        luceneQuery =
            queryParser.parse(QueryParserBase.escape(pfsQuery.toString()));

      } catch (ParseException e2) {

        Logger.getLogger(getClass()).info("  query = " + pfsQuery.toString());
        throw new LocalException(
            "The specified search terms cannot be parsed.  Please check syntax and try again.");
      }

      // If we get here, the query is fine.
    }

    // Validate query terms
    luceneQuery =
        luceneQuery.rewrite(fullTextEntityManager.getSearchFactory()
            .getIndexReaderAccessor().open(clazz));
    Set<Term> terms = new HashSet<>();
    luceneQuery.extractTerms(terms);
    for (Term t : terms) {
      if (t.field() != null && !t.field().isEmpty()
          && !allFieldNames.get(fieldNamesKey).contains(t.field())) {
        throw new Exception("Query references invalid field name " + t.field()
            + ", " + allFieldNames.get(fieldNamesKey));
      }
    }

    fullTextQuery =
        fullTextEntityManager.createFullTextQuery(luceneQuery, clazz);

    if (pfs != null) {
      // if start index and max results are set, set paging
      if (pfs.getStartIndex() >= 0 && pfs.getMaxResults() >= 0) {
        fullTextQuery.setFirstResult(pfs.getStartIndex());
        fullTextQuery.setMaxResults(pfs.getMaxResults());
      }

      // if sort field is specified, set sort key
      if (pfs.getSortField() != null && !pfs.getSortField().isEmpty()) {
        Map<String, Boolean> nameToAnalyzedMap =
            getNameAnalyzedPairsFromAnnotation(clazz, pfs.getSortField());
        String sortField = null;

        if (nameToAnalyzedMap.size() == 0) {
          throw new Exception(clazz.getName()
              + " does not have declared, annotated method for field "
              + pfs.getSortField());
        }

        // first check the default name (rendered as ""), if not analyzed, use
        // this as sort
        if (nameToAnalyzedMap.get("") != null
            && nameToAnalyzedMap.get("").equals(false)) {
          sortField = pfs.getSortField();
        }

        // otherwise check explicit [name]Sort index
        else if (nameToAnalyzedMap.get(pfs.getSortField() + "Sort") != null
            && nameToAnalyzedMap.get(pfs.getSortField() + "Sort").equals(false)) {
          sortField = pfs.getSortField() + "Sort";
        }

        // if none, throw exception
        if (sortField == null) {
          throw new Exception(
              "Could not retrieve a non-analyzed Field annotation for get method for variable name "
                  + pfs.getSortField());
        }

        Sort sort =
            new Sort(new SortField(sortField, SortField.Type.STRING,
                !pfs.isAscending()));
        fullTextQuery.setSort(sort);
      }
    }
    return fullTextQuery;
  }
  
  /**
   * Returns the name analyzed pairs from annotation.
   *
   * @param clazz the clazz
   * @param sortField the sort field
   * @return the name analyzed pairs from annotation
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   */
  public static Map<String, Boolean> getNameAnalyzedPairsFromAnnotation(
    Class<?> clazz, String sortField) throws NoSuchMethodException,
    SecurityException {
    final String key = clazz.getName() + "." + sortField;
    if (sortFieldAnalyzedMap.containsKey(key)) {
      return sortFieldAnalyzedMap.get(key);
    }

    // initialize the name->analyzed pair map
    Map<String, Boolean> nameAnalyzedPairs = new HashMap<>();

    Method m =
        clazz.getMethod("get" + sortField.substring(0, 1).toUpperCase()
            + sortField.substring(1), new Class<?>[] {});

    Set<org.hibernate.search.annotations.Field> annotationFields =
        new HashSet<>();

    // check for Field annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
      annotationFields.add(m
          .getAnnotation(org.hibernate.search.annotations.Field.class));
    }

    // check for Fields annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
      // add all specified fields
      for (org.hibernate.search.annotations.Field f : m.getAnnotation(
          org.hibernate.search.annotations.Fields.class).value()) {
        annotationFields.add(f);
      }
    }

    // cycle over discovered fields and put name and analyze == YES into map
    for (org.hibernate.search.annotations.Field f : annotationFields) {
      nameAnalyzedPairs.put(f.name(), f.analyze().equals(Analyze.YES) ? true
          : false);
    }

    sortFieldAnalyzedMap.put(key, nameAnalyzedPairs);

    return nameAnalyzedPairs;
  }
}
