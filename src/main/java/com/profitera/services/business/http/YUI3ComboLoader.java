package com.profitera.services.business.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.profitera.io.TransferWithBuffer;
import com.profitera.services.business.http.impl.CssRewriter;
import com.profitera.services.business.http.impl.ServletUtil;

public class YUI3ComboLoader extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Map<String, byte[]> CACHE = new ConcurrentHashMap<String, byte[]>();
  private static final String cacheExp;
  static {
    Calendar c = Calendar.getInstance();
    c.add(1, Calendar.YEAR);
    cacheExp = ServletUtil.getHttpDateFormat().format(c.getTime());
  }
  
  private final File yuiArchive;
  
  public YUI3ComboLoader(File yuiBuildZip) {
    yuiArchive = yuiBuildZip;
    if (!yuiBuildZip.exists()) {
      throw new IllegalArgumentException("Unable to start YUI3 Combo loader, invalid archive: " + yuiBuildZip.getAbsolutePath());
    }
  }
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    String pathString = request.getQueryString();
    if (pathString == null || pathString.length() == 0 || pathString.startsWith("t=")) {
      // If a single file is requested treat it as a 1-file combination load
      // If the query string was t=<some number> we ignore it because that is just to trick a cache
      String servletPath = "version/build" + request.getServletPath();
      pathString = servletPath;
    }
//    System.out.println(pathString);
    String[] fileRefs = getPathsToCombine(pathString);
    OutputStream outStream = response.getOutputStream();
    outStream = ServletUtil.wrapGzip(outStream, request, response);
    response.setHeader("Expires", cacheExp);
    boolean isCss = false;
    if (fileRefs.length > 0) {
      if (fileRefs[0].endsWith(".css")) {
        response.setContentType("text/css");
        isCss = true;
      } else if (fileRefs[0].endsWith(".js")) {
        response.setContentType("application/x-javascript");
      }
    }
    for (String path : fileRefs) {
      if (path.endsWith(".css") || path.endsWith(".js")) {
        if (CACHE.containsKey(path)) {
          outStream.write(CACHE.get(path));
        } else {
          byte[] content = retrieveFromYuiArchive(path);
          if (isCss) {
            content = new CssRewriter().rewriteUrlPaths(content, path);
          }
          outStream.write(content);
          CACHE.put(path, content);
        }
      } else {
        // Anything that is not css or JS is just sent as bytes, usually images
        byte[] content = retrieveFromYuiArchive(path);
        outStream.write(content);
      }
    }
    outStream.close();
  }

  private String[] getPathsToCombine(String pathString) {
    if (pathString.contains("&")) {
      return pathString.split("&");
    } else {
      return new String[] { pathString };
    }
  }

  private byte[] retrieveFromYuiArchive(String fileRef) throws ZipException,
      IOException, FileNotFoundException {
    ZipFile zf = new ZipFile(yuiArchive);
    try {
      String substring = fileRef.substring(fileRef.indexOf("/") + 1);
      if (!substring.startsWith("build")) {
        substring = "build/" + substring;
      }
      Pattern p = Pattern.compile("build(/\\d+\\.\\d+\\.\\d+)(.*)");
      Matcher matcher = p.matcher(substring);
      if (matcher.matches()) {
        substring = "build" + matcher.group(2);
      }
      String zipEntryPath = "yui/" + substring;
      ZipEntry entry = zf.getEntry(zipEntryPath);
      InputStream in = null;
      try {
        in = zf.getInputStream(entry);
      } catch (FileNotFoundException fnfe) {
        throw fnfe;
      }
      TransferWithBuffer twb = new TransferWithBuffer();
      ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
      twb.transfer(in, out, new byte[1024]);
      in.close();
      byte[] content = out.toByteArray();
      return content;
    } finally {
      zf.close();
    }
  }
  
  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) 
  throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    resp.getOutputStream().close();
  }

}