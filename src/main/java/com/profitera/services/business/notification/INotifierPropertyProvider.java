package com.profitera.services.business.notification;

/**
 * @author jamison
 */
public interface INotifierPropertyProvider {
  public abstract String getNotifierProperty(String notifierCode, String propertySuffix, String defaultValue);

  public abstract int getNotifierIntegerProperty(String notifierCode, String propertySuffix, int defaultValue);
}