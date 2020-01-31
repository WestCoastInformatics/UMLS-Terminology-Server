package com.wci.umls.server.jpa.algo.invert;

public class TestCase {

  private String shortName = "";
  
  private String name = "";
  
  private String failureMsg = "";
  
  private int errorCt = 0;
  
  public TestCase(String name) {
    this.setName(name);
  }
  
  public TestCase(String shortName, String name, String failureMsg) {
    this.setShortName(shortName);
    this.setName(name);
    this.setFailureMsg(failureMsg);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getFailureMsg() {
    return failureMsg;
  }

  public void setFailureMsg(String failureMsg) {
    this.failureMsg = failureMsg;
  }

  public int getErrorCt() {
    return errorCt;
  }

  public void setErrorCt(int errorCt) {
    this.errorCt = errorCt;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + errorCt;
    result =
        prime * result + ((failureMsg == null) ? 0 : failureMsg.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TestCase other = (TestCase) obj;
    if (errorCt != other.errorCt)
      return false;
    if (failureMsg == null) {
      if (other.failureMsg != null)
        return false;
    } else if (!failureMsg.equals(other.failureMsg))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (shortName == null) {
      if (other.shortName != null)
        return false;
    } else if (!shortName.equals(other.shortName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TestCase [shortName=" + shortName + ", name=" + name
        + ", failureMsg=" + failureMsg + ", errorCt=" + errorCt + "]";
  }

}
