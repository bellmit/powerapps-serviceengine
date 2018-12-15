package com.profitera.persistence;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;

public class LoggingWriter extends Writer {

  private final Log log;
  private StringBuffer buffer = new StringBuffer();
  public LoggingWriter(Log l){
    this.log = l;
    
  }
  public void close() throws IOException {
    // Do Nothing

  }

  public void flush() throws IOException {
    // Do nothing

  }
  public void write(char[] cbuf, int off, int len) throws IOException {
    for (int i = off; i < len; i++) {
      if (cbuf[i] == '\n'){
        buffer.append(cbuf, off, i - off);
        log.debug(buffer.toString());
        buffer.delete(0, buffer.length());
        int newStart = i + 1; 
        if (newStart >= cbuf.length){
          return;
        }
        int newLength = len - newStart;
        if (newLength < 0){
          return;
        }
        // I should be able to call this method recursively, but I just can't
        // get it right for some reason. This is less efficient, but works.
        char[] chars = new char[newLength];
        System.arraycopy(cbuf, newStart, chars, 0, newLength);
        write(chars);
        return;
        
      }
    }
    buffer.append(cbuf, off, len);
  }
}
