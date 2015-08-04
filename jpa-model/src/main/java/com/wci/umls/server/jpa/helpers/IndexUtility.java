/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Performs utility functions relating to Lucene indexes and Hibernate Search.
 */
public class IndexUtility {

  /**
   * Returns the indexed field names for a given class.
   *
   * @param clazz the clazz
   * @param stringOnly the string only flag
   * @return the indexed field names
   * @throws Exception the exception
   */
  public static Set<String> getIndexedStringFieldNames(Class<?> clazz,
    boolean stringOnly) throws Exception {

    // Avoid ngram and sort fields (these have special uses)
    Set<String> exclusions = new HashSet<>();
    exclusions.add("Sort");
    exclusions.add("nGram");
    exclusions.add("NGram");

    // When looking for default fields, exclude definitions and branches
    Set<String> stringExclusions = new HashSet<>();
    stringExclusions.add("definitions");
    stringExclusions.add("branch");

    Set<String> fieldNames = new HashSet<>();

    // first cycle over all methods
    for (Method m : clazz.getMethods()) {

      // if no annotations, skip
      if (m.getAnnotations().length == 0) {
        continue;
      }

      // check for @IndexedEmbedded
      if (m.isAnnotationPresent(IndexedEmbedded.class)) {
        throw new Exception(
            "Unable to handle @IndexedEmbedded on methods, specify on field");
      }

      // determine if there's a fieldBridge (which converts the field)
      boolean hasFieldBridge = false;
      if (m.isAnnotationPresent(Field.class)) {
        if (!m.getAnnotation(Field.class).bridge().impl().toString()
            .equals("void")) {
          hasFieldBridge = true;
        }
      }

      // for non-embedded fields, only process strings
      // This is because we're handling string based query here
      // Other fields can always be used with fielded query clauses
      if (stringOnly && !hasFieldBridge
          && !m.getReturnType().equals(String.class)) {
        continue;
      }

      // check for @Field annotation
      if (m.isAnnotationPresent(Field.class)) {
        String fieldName =
            getFieldNameFromMethod(m, m.getAnnotation(Field.class));
        fieldNames.add(fieldName);
      }

      // check for @Fields annotation
      if (m.isAnnotationPresent(Fields.class)) {
        for (Field field : m.getAnnotation(Fields.class).value()) {
          String fieldName = getFieldNameFromMethod(m, field);

          fieldNames.add(fieldName);
        }
      }
    }

    // second cycle over all fields
    for (java.lang.reflect.Field f : getAllFields(clazz)) {
      // check for @IndexedEmbedded
      if (f.isAnnotationPresent(IndexedEmbedded.class)) {

        // Assumes field is a collection, and has a OneToMany, ManyToMany, or
        // ManyToOne
        // annotation
        Class<?> jpaType = null;
        if (f.isAnnotationPresent(OneToMany.class)) {
          jpaType = f.getAnnotation(OneToMany.class).targetEntity();
        } else if (f.isAnnotationPresent(ManyToMany.class)) {
          jpaType = f.getAnnotation(ManyToMany.class).targetEntity();
        } else if (f.isAnnotationPresent(ManyToOne.class)) {
          jpaType = f.getAnnotation(ManyToOne.class).targetEntity();
        } else {
          throw new Exception(
              "Unable to determine jpa type, @IndexedEmbedded must be used with @OneToMany, @ManyToOne, or @ManyToMany ");

        }

        for (String embeddedField : getIndexedStringFieldNames(jpaType,
            stringOnly)) {
          fieldNames.add(f.getName() + "." + embeddedField);
        }
      }

      // determine if there's a fieldBridge (which converts the field)
      boolean hasFieldBridge = false;
      if (f.isAnnotationPresent(Field.class)) {
        if (f.getAnnotation(Field.class).bridge().impl().toString()
            .equals("void")) {
          hasFieldBridge = true;
        }
      }

      // for non-embedded fields, only process strings
      if (stringOnly && !hasFieldBridge && !f.getType().equals(String.class))
        continue;

      // check for @Field annotation
      if (f.isAnnotationPresent(Field.class)) {
        String fieldName =
            getFieldNameFromField(f, f.getAnnotation(Field.class));
        fieldNames.add(fieldName);
      }

      // check for @Fields annotation
      if (f.isAnnotationPresent(Fields.class)) {
        for (Field field : f.getAnnotation(Fields.class).value()) {
          String fieldName = getFieldNameFromField(f, field);
          fieldNames.add(fieldName);
        }
      }

    }

    // Apply filters
    Set<String> filteredFieldNames = new HashSet<>();
    OUTER: for (String fieldName : fieldNames) {
      for (String exclusion : exclusions) {
        if (fieldName.contains(exclusion)) {
          continue OUTER;
        }
      }
      for (String exclusion : stringExclusions) {
        if (stringOnly && fieldName.contains(exclusion)) {
          continue OUTER;
        }
      }
      filteredFieldNames.add(fieldName);
    }

    return filteredFieldNames;
  }

  /**
   * Helper function to get a field name from a method and annotation.
   *
   * @param m the reflected, annotated method, assumed to be of form
   *          getFieldName()
   * @param annotationField the annotation field
   * @return the indexed field name
   */
  private static String getFieldNameFromMethod(Method m, Field annotationField) {
    // iannotationField annotationFieldield has a speciannotationFieldied name,
    // use that
    if (annotationField.name() != null && !annotationField.name().isEmpty())
      return annotationField.name();

    // otherwise, assume method name of form getannotationFieldName
    // where the desired value is annotationFieldName
    if (m.getName().startsWith("get")) {
      return StringUtils.uncapitalize(m.getName().replace("get", ""));
    } else if (m.getName().startsWith("is")) {
      return StringUtils.uncapitalize(m.getName().replace("is", ""));
    } else
      return m.getName();

  }

  /**
   * Helper function get a field name from reflected Field and annotation.
   *
   * @param annotatedField the reflected, annotated field
   * @param annotationField the field annotation
   * @return the indexed field name
   */
  private static String getFieldNameFromField(
    java.lang.reflect.Field annotatedField, Field annotationField) {
    if (annotationField.name() != null && !annotationField.name().isEmpty()) {
      return annotationField.name();
    }

    return annotatedField.getName();
  }

  /**
   * Returns the all fields.
   *
   * @param type the type
   * @return the all fields
   */
  private static java.lang.reflect.Field[] getAllFields(Class<?> type) {
    if (type.getSuperclass() != null) {
      return ArrayUtils.addAll(getAllFields(type.getSuperclass()),
          type.getDeclaredFields());
    }
    return type.getDeclaredFields();
  }

}
