package com.powerapps;

import java.net.URL;

import org.junit.Test;

import com.powerapps.http.helpers.FileHelper;

public class FileHelperTest {
  
  private String RESOURCE_PATH = "../serverconfig/"; 
  
  
  @Test
  public void class_path_resource_cannot_be_null() {
    URL url = FileHelper.getClassPathResource("wlt.getAccount".concat(".event"));
    System.out.println(url.toString());
  }

}
