/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.model.meta.ContactInfo;

/**
 * JPA and JAXB enabled implementation of {@link ContactInfo}.
 */
@Entity
@Table(name = "contact_info")
@Audited
@XmlRootElement(name = "contactInfo")
public class ContactInfoJpa implements ContactInfo {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The address1. */
  @Column(nullable = true)
  private String address1;

  /** The address2. */
  @Column(nullable = true)
  private String address2;

  /** The city. */
  @Column(nullable = true)
  private String city;

  /** The country. */
  @Column(nullable = true)
  private String country;

  /** The email. */
  @Column(nullable = true)
  private String email;

  /** The fax. */
  @Column(nullable = true)
  private String fax;

  /** The name. */
  @Column(nullable = true)
  private String name;

  /** The organization. */
  @Column(nullable = true)
  private String organization;

  /** The state or province. */
  @Column(nullable = true)
  private String stateOrProvince;

  /** The telephone. */
  @Column(nullable = true)
  private String telephone;

  /** The title. */
  @Column(nullable = true)
  private String title;

  /** The url. */
  @Column(nullable = true)
  private String url;

  /** The value. */
  @Column(nullable = true)
  private String value;

  /** The zip code. */
  @Column(nullable = true)
  private String zipCode;

  /**
   * Instantiates an empty {@link ContactInfoJpa}.
   */
  public ContactInfoJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ContactInfoJpa} from the specified parameters.
   *
   * @param contactInfo the i
   */
  public ContactInfoJpa(ContactInfo contactInfo) {
    address1 = contactInfo.getAddress1();
    address2 = contactInfo.getAddress2();
    city = contactInfo.getCity();
    country = contactInfo.getCountry();
    email = contactInfo.getEmail();
    fax = contactInfo.getFax();
    name = contactInfo.getName();
    organization = contactInfo.getOrganization();
    stateOrProvince = contactInfo.getStateOrProvince();
    telephone = contactInfo.getTelephone();
    title = contactInfo.getTitle();
    url = contactInfo.getUrl();
    value = contactInfo.getValue();
    zipCode = contactInfo.getZipCode();
  }

  /**
   * Instantiates a {@link ContactInfoJpa} from the specified parameters.
   *
   * @param mrsabField the mrsab field
   */
  public ContactInfoJpa(String mrsabField) {
    // 0 John Kilbourne, M.D. ;
    // 1 Head, MeSH Section;
    // 2 National Library of Medicine;
    // 3 6701 Democracy Blvd.;
    // 4 Suite 202 MSC 4879;
    // 5 Bethesda;
    // 6 Maryland;
    // 7 United States;
    // 8 20892-4879;
    // 9 kilbourj@mail.nlm.nih.gov
    String[] fields = FieldedStringTokenizer.split(mrsabField, ";");
    if (fields.length < 10) {
      // does not meet requirements, bail
      this.value = mrsabField;
      return;
    }
    name = fields[0];
    title = fields[1];
    organization = fields[2];
    address1 = fields[3];
    address2 = fields[4];
    city = fields[5];
    stateOrProvince = fields[6];
    country = fields[7];
    zipCode = fields[8];
    email = fields[9];
  }

  /* see superclass */
  @Override
  public Long getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public String getAddress1() {
    return address1;
  }

  /* see superclass */
  @Override
  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  /* see superclass */
  @Override
  public String getAddress2() {
    return address2;
  }

  /* see superclass */
  @Override
  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  /* see superclass */
  @Override
  public String getCity() {
    return city;
  }

  /* see superclass */
  @Override
  public void setCity(String city) {
    this.city = city;
  }

  /* see superclass */
  @Override
  public String getCountry() {
    return country;
  }

  /* see superclass */
  @Override
  public void setCountry(String country) {
    this.country = country;
  }

  /* see superclass */
  @Override
  public String getEmail() {
    return email;
  }

  /* see superclass */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /* see superclass */
  @Override
  public String getFax() {
    return fax;
  }

  /* see superclass */
  @Override
  public void setFax(String fax) {
    this.fax = fax;
  }

  /* see superclass */
  @Override
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getOrganization() {
    return organization;
  }

  /* see superclass */
  @Override
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /* see superclass */
  @Override
  public String getStateOrProvince() {
    return stateOrProvince;
  }

  /* see superclass */
  @Override
  public void setStateOrProvince(String stateOrProvince) {
    this.stateOrProvince = stateOrProvince;
  }

  /* see superclass */
  @Override
  public String getTelephone() {
    return telephone;
  }

  /* see superclass */
  @Override
  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  /* see superclass */
  @Override
  public String getTitle() {
    return title;
  }

  /* see superclass */
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  /* see superclass */
  @Override
  public String getUrl() {
    return url;
  }

  /* see superclass */
  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  /* see superclass */
  @Override
  public String getValue() {
    return value;
  }

  /* see superclass */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* see superclass */
  @Override
  public String getZipCode() {
    return zipCode;
  }

  /* see superclass */
  @Override
  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address1 == null) ? 0 : address1.hashCode());
    result = prime * result + ((address2 == null) ? 0 : address2.hashCode());
    result = prime * result + ((city == null) ? 0 : city.hashCode());
    result = prime * result + ((country == null) ? 0 : country.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((fax == null) ? 0 : fax.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((organization == null) ? 0 : organization.hashCode());
    result =
        prime * result
            + ((stateOrProvince == null) ? 0 : stateOrProvince.hashCode());
    result = prime * result + ((telephone == null) ? 0 : telephone.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ContactInfoJpa other = (ContactInfoJpa) obj;
    if (address1 == null) {
      if (other.address1 != null)
        return false;
    } else if (!address1.equals(other.address1))
      return false;
    if (address2 == null) {
      if (other.address2 != null)
        return false;
    } else if (!address2.equals(other.address2))
      return false;
    if (city == null) {
      if (other.city != null)
        return false;
    } else if (!city.equals(other.city))
      return false;
    if (country == null) {
      if (other.country != null)
        return false;
    } else if (!country.equals(other.country))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (fax == null) {
      if (other.fax != null)
        return false;
    } else if (!fax.equals(other.fax))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (organization == null) {
      if (other.organization != null)
        return false;
    } else if (!organization.equals(other.organization))
      return false;
    if (stateOrProvince == null) {
      if (other.stateOrProvince != null)
        return false;
    } else if (!stateOrProvince.equals(other.stateOrProvince))
      return false;
    if (telephone == null) {
      if (other.telephone != null)
        return false;
    } else if (!telephone.equals(other.telephone))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (zipCode == null) {
      if (other.zipCode != null)
        return false;
    } else if (!zipCode.equals(other.zipCode))
      return false;
    return true;
  }

}
