/*
 * Created on Mar 23, 2005
 */
package com.profitera.descriptor.db.reference;

import java.io.Serializable;

/**
 * @author jambu
 */
public class ProfileCodeRef implements Serializable {
  // Generated constants
  public static final String CODE = "code";
  public static final String DESCRIPTION = "description";
  public static final String DISABLE = "disable";
  public static final String ID = "id";
  public static final String SORT_PRIORITY = "sortPriority";
  // End of generated constants

  private java.lang.String code;
  private java.lang.String description;
  private java.lang.Short disable;
  private java.lang.Long id;
  private java.lang.Integer sortPriority;

  public java.lang.String getCode() {
    return code;
  }

  public java.lang.String getDescription() {
    return description;
  }

  public java.lang.Short getDisable() {
    return disable;
  }

  public java.lang.Long getId() {
    return id;
  }

  public java.lang.Integer getSortPriority() {
    return sortPriority;
  }

  public void setCode(java.lang.String code) {
    this.code = code;
  }

  public void setDescription(java.lang.String description) {
    this.description = description;
  }

  public void setDisable(java.lang.Short disable) {
    this.disable = disable;
  }

  public void setId(java.lang.Long id) {
    this.id = id;
  }

  public void setSortPriority(java.lang.Integer sortPriority) {
    this.sortPriority = sortPriority;
  }
}