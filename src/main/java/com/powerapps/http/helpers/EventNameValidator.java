package com.powerapps.http.helpers;

import java.io.File;
import java.net.URL;

import com.kollect.etl.util.StringUtils;

public class EventNameValidator {
  
  
  private static final String CARMEL_CASE_NAME_PATTERN="^[a-z]+.[a-z]+|[A-Z][a-z]+$";
  
  private StringUtils stringUtils;
  
  public EventNameValidator(StringUtils stringUtils) {
    this.stringUtils = stringUtils;
  }

  
  public boolean isValid(final String eventName) {
    
    if(!eventNameIsValid(eventName)) {
      System.err.println("Invalid event name");
      return false;
    }else if (!eventExistsInClassPath(eventName)) {
      System.err.println("Event doesn't exists");
      return false;
    }
    return true;
  }
  
  public boolean eventExistsInClassPath(String eventName) {
    eventName = eventName.concat(".event");
    URL url = FileHelper.getClassPathResource(eventName);
    //checking for null here because i did not create this URL object myself.
    if(url != null) {
      String fileName = url.getFile();
      System.out.println(fileName);
      if(new File(url.getFile()).exists()) {
        return true;
      }
    }
    return false;
  }
  
  
  public boolean eventNameIsValid(String eventName) {
    System.out.println(eventName);
    return stringUtils.hasMatch(eventName, CARMEL_CASE_NAME_PATTERN);
  }

}
