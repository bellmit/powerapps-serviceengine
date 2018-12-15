package com.profitera.services.system.document;

import java.io.File;
import java.io.IOException;

import com.profitera.services.system.document.impl.IDocumentLineListener;

public interface IDocumentFileVerifier {

  public abstract void verify(File source, IDocumentLineListener listener)
      throws IOException;

}