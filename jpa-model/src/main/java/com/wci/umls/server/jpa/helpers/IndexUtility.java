/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * The Class IndexUtility Performs utility functions relating to Lucene indexes
 * and Hibernate Search
 */
public class IndexUtility {

  /**
   * Returns the indexed field names for a given class
   *
   * @param clazz the clazz
   * @return the indexed field names
   */
  public static List<String> getIndexedStringFieldNames(Class<?> clazz) {

    List<String> fieldNames = new ArrayList<>();

    // first cycle over all methods
    for (Method m : clazz.getMethods()) {

      // if no annotations, skip
      if (m.getAnnotations().length == 0) {
        continue;
      }

      // check for @IndexEmbedded
      if (m.isAnnotationPresent(IndexedEmbedded.class)) {
        IndexedEmbedded embedded = m.getAnnotation(IndexedEmbedded.class);
        for (String embeddedField : getIndexedStringFieldNames(embedded
            .targetElement())) {
          fieldNames.add(getClassFieldName(embedded.targetElement()) + "."
              + embeddedField);
        }
      }

      // for non-embedded fields, only process strings
      if (!m.getReturnType().equals(String.class))
        continue;

      // check for @Field annotation
      if (m.isAnnotationPresent(Field.class)) {
        String fieldName =
            getFieldNameFromMethod(m, m.getAnnotation(Field.class));

        // skip Sort fields
        if (!fieldName.endsWith("Sort"))
          fieldNames.add(fieldName);
      }

      // check for @Fields annotation
      if (m.isAnnotationPresent(Fields.class)) {
        for (Field field : m.getAnnotation(Fields.class).value()) {
          String fieldName = getFieldNameFromMethod(m, field);

          // skip Sort fields
          if (!fieldName.endsWith("Sort"))
            fieldNames.add(fieldName);
        }
      }
    }

    // second cycle over all fields
    for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {

      // check for @IndexEmbedded
      if (f.isAnnotationPresent(IndexedEmbedded.class)) {
        IndexedEmbedded embedded = f.getAnnotation(IndexedEmbedded.class);
        for (String embeddedField : getIndexedStringFieldNames(embedded
            .targetElement())) {
          fieldNames.add(f.getName() + "." + embeddedField);
        }
      }

      // for non-embedded fields, only process strings
      if (!f.getType().equals(String.class))
        continue;

      // check for @Field annotation
      if (f.isAnnotationPresent(Field.class)) {
        String fieldName =
            getFieldNameFromField(f, f.getAnnotation(Field.class));

        // skip Sort fields
        if (!fieldName.endsWith("Sort"))
          fieldNames.add(fieldName);
      }

      // check for @Fields annotation
      if (f.isAnnotationPresent(Fields.class)) {
        for (Field field : f.getAnnotation(Fields.class).value()) {

          String fieldName = getFieldNameFromField(f, field);

          // skip Sort fields
          if (!fieldName.endsWith("Sort"))
            fieldNames.add(fieldName);
        }
      }

    }

    return fieldNames;
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
    return StringUtils.uncapitalize(m.getName().replace("get", ""));

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
   * Helper function to get the base object name without Jpa.
   *
   * @param clazz the class
   * @return the class field name
   */
  private static String getClassFieldName(Class<?> clazz) {
    return StringUtils
        .uncapitalize(clazz.getSimpleName().replaceAll("Jpa", ""));
  }
}
