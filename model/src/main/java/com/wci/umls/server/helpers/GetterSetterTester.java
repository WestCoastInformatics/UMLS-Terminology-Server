package com.wci.umls.server.helpers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Automates JUnit testing of simple getter/setter methods.
 * 
 * <p>
 * It may be used in exclusive or inclusive mode. In exclusive mode, which is
 * the default, all JavaBeans properties (getter/setter method pairs with
 * matching names) are tested unless they are excluded beforehand. For example:
 * 
 * <pre>
 * MyClass objectToTest = new MyClass();
 * GetterSetterTester gst = new GetterSetterTester(objectToTest);
 * gst.exclude(&quot;complexProperty&quot;);
 * gst.exclude(&quot;anotherProperty&quot;);
 * gst.test();
 * </pre>
 * 
 * <p>
 * In inclusive mode, only properties that are explicitly listed are tested. For
 * example:
 * 
 * <pre>
 * new GetterSetterTester(new MyClass()).include(&quot;aSimpleProperty&quot;)
 *     .include(&quot;secondProperty&quot;).test();
 * </pre>
 * 
 * <p>
 * The second example also illustrates how to call this class in as terse a way
 * as possible.
 * 
 * <p>
 * The following property types are supported:
 * 
 * <ul>
 * <li>All Java primitive types.
 * <li>Interfaces.
 * <li>All non-final classes if <a href="http://cglib.sourceforge.net">cglib</a>
 * is on your classpath -- this uses cglib even when a no-argument constructor
 * is available because a constructor might have side effects that you wouldn.t
 * want to trigger in a unit test.
 * <li>Java 5 enums.
 * </ul>
 * 
 * <p>
 * Properties whose types are classes declared <code>final</code> are not
 * supported; neither are non-primitive, non-interface properties if you don't
 * have cglib.
 * 
 * <p>
 * Copyright (c) 2005, Steven Grimm.<br>
 * This software may be used for any purpose, commercial or noncommercial, so
 * long as this copyright notice is retained. If you make improvements to the
 * code, you're encouraged (but not required) to send them to me so I can make
 * them available to others. For updates, please check <a
 * href="http://www.plaintivemewling.com/?p=34">here</a>.
 * 
 * @author Steven Grimm <a
 *         href="mailto:koreth@midwinter.com">koreth@midwinter.com</a>
 * @version 1.0 (2005/11/08).
 */
public class GetterSetterTester {
  /** Object under test. */
  private Object obj;

  /** Class of object under test. */
  private Class<?> clazz;

  /** Set of fields to exclude. */
  private Set<String> excludes = new TreeSet<String>();

  /** Set of fields to include. */
  private Set<String> includes = null;

  /** If true, output trace information. */
  private boolean verbose = false;

  /**
   * Constructs a new getter/setter tester to test objects of a particular
   * class.
   * 
   * @param obj Object to test.
   */
  public GetterSetterTester(Object obj) {
    this.obj = obj;
    this.clazz = obj.getClass();
  }

  /**
   * Adds a field to the list of tested fields. If this method is called, the
   * tester will not attempt to list all the getters and setters on the object
   * under test, and will instead simply test all t he fields in the include
   * list.
   * 
   * @param field Field name whose getter/setter should be tested.
   * @return This object, so include calls can be chained together.
   */
  public GetterSetterTester include(String field) {
    if (includes == null)
      includes = new TreeSet<String>();
    includes.add(field.toLowerCase());
    return this;
  }

  /**
   * Adds a field to the list of excluded fields.
   * 
   * @param field Field name to exclude from testing.
   * @return This object, so exclude calls can be chained together.
   */
  public GetterSetterTester exclude(String field) {
    excludes.add(field.toLowerCase());
    return this;
  }

  /**
   * Sets the verbosity flag.
   * @param verbose the verbose flag
   * @return this
   */
  public GetterSetterTester setVerbose(boolean verbose) {
    this.verbose = verbose;
    return this;
  }

  /**
   * Walks through the methods in the class looking for getters and setters that
   * are on our include list (if any) and are not on our exclude list.
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws ClassNotFoundException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InstantiationException
   * @throws InvocationTargetException
   */
  public void test() throws InvocationTargetException,
    IllegalArgumentException, IllegalAccessException, SecurityException,
    ClassNotFoundException, NoSuchMethodException, InstantiationException {
    Method[] methods = clazz.getMethods();

    for (int i = 0; i < methods.length; i++) {
      /* We're looking for single-argument setters. */
      Method m = methods[i];
      if (!m.getName().startsWith("set"))
        continue;
      String fieldName = m.getName().substring(3);
      Class<?>[] args = m.getParameterTypes();
      if (args.length != 1)
        continue;

      /* Check the field name against our include/exclude list. */
      if (includes != null && !includes.contains(fieldName.toLowerCase())) {
        continue;
      }
      if (excludes.contains(fieldName.toLowerCase()))
        continue;

      /* Is there a getter that returns the same type? */
      Method getter;
      try {
        getter = clazz.getMethod("get" + fieldName, new Class[] {});
        if (getter.getReturnType() != args[0])
          continue;
      } catch (NoSuchMethodException e) {
        try {
          getter = clazz.getMethod("is" + fieldName, new Class[] {});
          if (getter.getReturnType() != args[0])
            continue;
        } catch (NoSuchMethodException e2) {
          continue;
        }
      }

      testGetterSetter(getter, m, args[0]);
    }
  }

  /**
   * Dummy invocation handler for our proxy objects.
   */
  class DummyInvocationHandler implements InvocationHandler {
    /**
     * @param o
     * @param m
     * @param a
     * @return object
     */
    @Override
    public Object invoke(Object o, Method m, Object[] a) {
      return null;
    }
  }

  /**
   * Tests a single getter/setter pair using an argument of a particular type.
   * @param get the get method
   * @param set the set method
   * @param argType the data type
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws NoSuchMethodException
   * @throws ClassNotFoundException
   * @throws SecurityException
   * @throws InstantiationException
   */
  private void testGetterSetter(Method get, Method set, Class<?> argType)
    throws InvocationTargetException, IllegalArgumentException,
    IllegalAccessException, SecurityException, ClassNotFoundException,
    NoSuchMethodException, InstantiationException {
    if (this.verbose)
      System.out.println("Testing " + get.getDeclaringClass().getName() + "."
          + get.getName());
    Object proxy = makeProxy(argType);
    try {
      set.invoke(this.obj, new Object[] {
        proxy
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      throw new RuntimeException("Setter " + set.getDeclaringClass().getName()
          + "." + set.getName() + " threw " + e.getTargetException().toString());
    }

    Object getResult;
    try {
      getResult = get.invoke(this.obj, new Object[] {});
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Setter " + set.getDeclaringClass().getName()
          + "." + set.getName() + " threw " + e.getTargetException().toString());
    }

    if (getResult == proxy || proxy.equals(getResult))
      return;
    throw new RuntimeException("Getter " + get.getName()
        + " did not return value from setter");
  }

  /**
   * Makes a proxy of a given class. If the class is an interface type, uses the
   * standard JDK proxy mechanism. If it's not, uses cglib. The use of cglib is
   * via reflection so that cglib is not required to use this library unless the
   * caller actually needs to proxy a concrete class.
   * @param type the type
   * @return a class of the specified type
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InstantiationException
   */
  @SuppressWarnings("rawtypes")
  private Object makeProxy(Class<?> type) throws ClassNotFoundException,
    SecurityException, NoSuchMethodException, IllegalArgumentException,
    IllegalAccessException, InvocationTargetException, InstantiationException {
    /* If it's a primitive type, just create it. */
    if (type == String.class)
      return "";
    if (type == Boolean.class || type == boolean.class)
      return new Boolean(false);
    if (type == Integer.class || type == int.class)
      return new Integer(0);
    if (type == Long.class || type == long.class)
      return new Long(0);
    if (type == Double.class || type == double.class)
      return new Double(0);
    if (type == Float.class || type == float.class)
      return new Float(0);
    if (type == Character.class || type == char.class)
      return new Character('x');
    if (type == BigDecimal.class)
      return new BigDecimal("0");
    if (type == Set.class)
      return new HashSet();
    if (type == BigInteger.class)
      return new BigInteger("0");
    // JAVA5 - Comment out or remove the next two lines on older Java versions.
    if (type.isEnum())
      return makeEnum(type);

    /* Use JDK dynamic proxy if the argument is an interface. */
    if (type.isInterface())
      return Proxy.newProxyInstance(type.getClassLoader(), new Class[] {
        type
      }, new DummyInvocationHandler());

    /* Get the CGLib classes we need. */
    Class<?> enhancerClass = null;
    Class<?> callbackClass = null;
    Class<?> fixedValueClass = null;
    try {
      enhancerClass = Class.forName("net.sf.cglib.proxy.Enhancer");
      callbackClass = Class.forName("net.sf.cglib.proxy.Callback");
      fixedValueClass = Class.forName("net.sf.cglib.proxy.FixedValue");
    } catch (ClassNotFoundException e) {
      throw new ClassNotFoundException("Need cglib to make a dummy "
          + type.getName() + ". Make sure cglib.jar is on " + "your classpath.");
    }

    /* Make a dummy callback (proxies within proxies!) */
    Object callback;
    callback =
        Proxy.newProxyInstance(callbackClass.getClassLoader(), new Class[] {
          fixedValueClass
        }, new DummyInvocationHandler());

    Method createMethod = enhancerClass.getMethod("create", new Class[] {
        Class.class, callbackClass
    });

    return createMethod.invoke(null, new Object[] {
        type, callback
    });
  }

  /**
   * Returns an instance of an enum.
   * 
   * JAVA5 - Comment out or remove this method on older Java versions.
   * @param clazz1 the class
   * @return an instance of an enum
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  @SuppressWarnings("static-method")
  private Object makeEnum(Class<?> clazz1) throws SecurityException,
    NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
    InvocationTargetException {
    Method m = clazz1.getMethod("values", new Class[0]);
    Object[] o = (Object[]) m.invoke(null, new Object[0]);
    return o[0];
  }
}
