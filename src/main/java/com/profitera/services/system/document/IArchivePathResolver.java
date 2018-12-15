package com.profitera.services.system.document;

import java.io.File;

public interface IArchivePathResolver {

  public abstract File getPath(long id);

}