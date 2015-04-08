package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.ContactInfo;

/**
 * JPA-enabled implementation of {@link ContactInfo}.
 */
@Entity
@Table(name = "contact_info", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "email"
}))
@Audited
@XmlRootElement(name = "contactInfo")
public class ContactInfoJpa implements ContactInfo {

  /** The id. */
  @Id
  @GeneratedValue
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getId()
   */
  @Override
  @XmlTransient
  public Long getId() {
    return this.id;
  }


  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getAddress1()
   */
  @Override
  public String getAddress1() {
    return address1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.ContactInfo#setAddress1(java.lang.String)
   */
  @Override
  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getAddress2()
   */
  @Override
  public String getAddress2() {
    return address2;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.ContactInfo#setAddress2(java.lang.String)
   */
  @Override
  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getCity()
   */
  @Override
  public String getCity() {
    return city;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setCity(java.lang.String)
   */
  @Override
  public void setCity(String city) {
    this.city = city;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getCountry()
   */
  @Override
  public String getCountry() {
    return country;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.ContactInfo#setCountry(java.lang.String)
   */
  @Override
  public void setCountry(String country) {
    this.country = country;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getEmail()
   */
  @Override
  public String getEmail() {
    return email;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setEmail(java.lang.String)
   */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getFax()
   */
  @Override
  public String getFax() {
    return fax;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setFax(java.lang.String)
   */
  @Override
  public void setFax(String fax) {
    this.fax = fax;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getOrganization()
   */
  @Override
  public String getOrganization() {
    return organization;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.ContactInfo#setOrganization(java.lang.String
   * )
   */
  @Override
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getStateOrProvince()
   */
  @Override
  public String getStateOrProvince() {
    return stateOrProvince;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.ContactInfo#setStateOrProvince(java.lang
   * .String)
   */
  @Override
  public void setStateOrProvince(String stateOrProvince) {
    this.stateOrProvince = stateOrProvince;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getTelephone()
   */
  @Override
  public String getTelephone() {
    return telephone;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.ContactInfo#setTelephone(java.lang.String)
   */
  @Override
  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getTitle()
   */
  @Override
  public String getTitle() {
    return title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setTitle(java.lang.String)
   */
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getUrl()
   */
  @Override
  public String getUrl() {
    return url;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setUrl(java.lang.String)
   */
  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getValue()
   */
  @Override
  public String getValue() {
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#setValue(java.lang.String)
   */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ContactInfo#getZipCode()
   */
  @Override
  public String getZipCode() {
    return zipCode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.ContactInfo#setZipCode(java.lang.String)
   */
  @Override
  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }
  

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
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

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
