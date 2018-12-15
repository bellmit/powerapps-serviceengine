package com.profitera.services.system.ptrsqlmap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Handler extends URLStreamHandler {
  public static Map<String, String> RESOURCE_MAP = Collections.synchronizedMap(new HashMap<String, String>());

  protected URLConnection openConnection(URL u) throws IOException {
    URLConnection c = new URLConnection(u) {
      public void connect() throws IOException {
        if (RESOURCE_MAP.get(getURL().getHost()) == null) {
          throw new IOException("No registered resource for host '" + getURL().getHost() + "'");
        }
      }

      public InputStream getInputStream() throws IOException {
        connect();
        return new ByteArrayInputStream(RESOURCE_MAP.get(getURL().getHost()).toString().getBytes());
      }
    };
    return c;
  }
}
