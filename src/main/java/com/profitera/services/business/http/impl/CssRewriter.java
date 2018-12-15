package com.profitera.services.business.http.impl;

import java.io.UnsupportedEncodingException;

public class CssRewriter {
  public byte[] rewriteUrlPaths(byte[] content, String contentPath) throws UnsupportedEncodingException {
    contentPath = contentPath.replaceAll("[0-9.pr]+/build/", "");
    StringBuilder sb = new StringBuilder(new String(content, "UTF8"));
    int pos = 0;
    while (true) {
      pos = sb.indexOf("url(", pos);
      if (pos == -1) {
        return sb.toString().getBytes("UTF8");
      } else {
        // Detect a data URI and skip:
        //.yui3-calendarnav-nextmonth{
        //url(data:image/png;base64,iVBORw0KGgoAAAANS...YII=)}
        String uriStart = sb.substring(pos, pos + 9);
        if (!uriStart.equals("url(data:")) {
          //Resolve all relative paths to something like this:
          // /yui/node-menunav/assets/skins/sam/vertical-menu-submenu-indicator.png
          sb.replace(pos, pos + 4, "url(/yui/" + contentPath.substring(0, contentPath.lastIndexOf('/') + 1));
        }
        pos++;
      }
    }
  }
}
