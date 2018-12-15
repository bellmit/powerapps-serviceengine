package com.powerapps.http.helpers;

import java.net.URL;

public class FileHelper {
  
  public static URL getClassPathResource(final String name) {
    Class<?> clazz = FileHelper.class;
    //Note: ClassLoader.getResource returns null as a valid response if the resource is not found.
    //The caller would throw NPE if null is returned and therefore mandates checking for nulls
    return clazz.getClassLoader().getResource(name);
    
  }

}
