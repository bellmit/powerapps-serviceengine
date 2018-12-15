package com.profitera.services.business.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.profitera.io.TransferWithBuffer;
import com.profitera.util.Utilities;

public class ServletUtil {
  public static final String JS_MIME = "application/x-javascript";
  public static final int BUFFER_SIZE = 1024 * 10; // 10KB
  public static OutputStream wrapGzip(OutputStream o, HttpServletRequest req, HttpServletResponse r) throws IOException {
    String accepts = req.getHeader("Accept-Encoding");
    boolean isGzippable = accepts.contains("gzip");
    if (isGzippable) {
      o = new GZIPOutputStream(o);
      r.setHeader("Content-Encoding","gzip");
    }
    return o;
  }

  public static void nocache(HttpServletResponse resp) {
    resp.setHeader("Cache-Control", "no-cache");
    resp.setHeader("Expires", "-1");
  }
  
  public static void redirectTo(HttpServletResponse resp, String loc) {
    resp.setHeader("Location", loc);
    resp.setStatus(HttpURLConnection.HTTP_MOVED_TEMP);
  }

  public static SimpleDateFormat getHttpDateFormat() {
    return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
  }
  
  public static void setExpires(HttpServletResponse resp, Integer calField, int addAmount) {
    if (calField == null) {
      nocache(resp);
    } else {
      Calendar c = Calendar.getInstance();
      c.add(calField, addAmount);
      resp.setHeader("Expires", getHttpDateFormat().format(c.getTime()));  
    }    
  }
  
  public static String getReferingLocalPage(HttpServletRequest req, HttpServlet s) {
    String referrer = req.getHeader("Referer");
    String page = null;
    if (!referrer.contains(s.getServletContext().getContextPath())) {
      return null;
    }
    String afterContext = getLocalReferingAfterContext(req, s);
    if (afterContext.startsWith("/")) {
      afterContext = afterContext.substring(1);
    }
    if (afterContext.endsWith("/")) {
      page = afterContext.substring(0, afterContext.lastIndexOf('/'));
    } else {
      page = afterContext;
    }
    if (page.contains("?")) {
      page = page.substring(0, page.indexOf('?'));
    }
    return page;
  }

  public static String getLocalReferingAfterContext(HttpServletRequest req,
      HttpServlet s) {
    String referrer = req.getHeader("Referer");
    int pathLength = s.getServletContext().getContextPath().length();
    int index = referrer.lastIndexOf(s.getServletContext().getContextPath());
    String afterContext = referrer.substring(index + pathLength);
    if (afterContext.startsWith("/")) {
      afterContext = afterContext.substring(1);
    }
    return afterContext;
  }

  public static void jsonHeaders(HttpServletResponse resp) {
    resp.setHeader("Content-Type", "application/json");
  }
  
  public static void pdfHeaders(HttpServletResponse resp) {
    resp.setHeader("Content-Type", "application/pdf");
  }
  
  public static void plainTextHeaders(HttpServletResponse resp) {
    resp.setHeader("Content-Type", "text/plain");
  }
  public static void plainTextHeaders(HttpServletResponse resp, Charset forName) {
    resp.setHeader("Content-Type", "text/plain; charset=" + forName.displayName());
  }


  public static boolean acceptsJson(HttpServletRequest req) {
    return req.getHeader("Accept").contains("application/json");
  }
  
  public static String findCookieValue(String name, HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if (cookies == null) {
      return null;
    }
    for (int i = 0; i < cookies.length; i++) {
      if (cookies[i].getName().equals(name)) {
        return cookies[i].getValue();
      }
    }
    return null;
  }

  public static void htmlHeader(HttpServletResponse resp) {
    contentType("text/html; charset=utf-8", resp);
  }

  public static void removeCookie(String name, HttpServletResponse resp) {
    Cookie c = new Cookie(name, "");
    c.setMaxAge(0);
    resp.addCookie(c);
  }

  public static void attachment(String fileName, HttpServletResponse resp) {
    disposition("attachment", fileName, resp);
  }

  public static void inline(String fileName, HttpServletResponse resp) {
    disposition("inline", fileName, resp);
  }
  
  private static void disposition(String mode, String fileName, HttpServletResponse resp) {
    String v = mode;
    if (fileName != null) {
      v = v + "; filename=" + fileName;
    }
    resp.setHeader("Content-Disposition", v);
  }

  public static void contentType(String contentType, HttpServletResponse resp) {
    resp.setHeader("Content-Type", contentType);
  }

  /**
   * @param resp
   * @param in
   *          Input stream of data is closed after sending
   * @throws IOException
   */
  public static void sendAsJavascript(HttpServletResponse resp, InputStream in, HttpServletRequest req)
      throws IOException {
    OutputStream out = wrapGzip(resp.getOutputStream(), req, resp);
    resp.setContentType(JS_MIME);
    setExpires(resp, Calendar.SECOND, 1);
    new TransferWithBuffer().transfer(in, out, new byte[BUFFER_SIZE]);
    in.close();
    out.close();
    resp.setStatus(HttpURLConnection.HTTP_OK);
  }

  public static void sendAsJavascriptFromClasspath(String resource, HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    URL r = Utilities.getResource(resource);
    sendAsJavascript(resp, r.openStream(), req);
  }

  public static void sendAsCss(InputStream fileInputStream, HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/css");
    setExpires(resp, Calendar.SECOND, 1);
    OutputStream o = wrapGzip(resp.getOutputStream(), req, resp);
    new TransferWithBuffer().transfer(fileInputStream, o, new byte[BUFFER_SIZE]);
    fileInputStream.close();
    resp.setStatus(HttpURLConnection.HTTP_OK);
    o.close();
  }

}
