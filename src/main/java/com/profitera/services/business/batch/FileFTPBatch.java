package com.profitera.services.business.batch;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.util.Strings;
import com.profitera.util.ftp.SynchronousFTPBean;

public class FileFTPBatch extends AbstractBatchProcess {
  
  public FileFTPBatch(){
    addRequiredProperty("sourcepath", String.class, "Source file location", "The path to the directory where the file(s) to be transferred are to be found");
    addRequiredProperty("sourcefile", String.class, "Source file name", "The name matcher for the file(s) to send to the target machine, this is not a traditional FTP globbing expression but a regex pattern");
    addProperty("targetpath", String.class, "", "Target file location", "The path to the directory on the remote machine where the file(s) will be sent");
    addRequiredProperty("user", String.class, "FTP user id", "The user id to use to log into the ftp server");
    addRequiredProperty("password", String.class, "FTP password", "The password for ftp server");
    addRequiredProperty("host", String.class, "FTP host name", "The host name of ip of the ftp server");
    addProperty("port", Integer.class, "21", "FTP port", "The port number that the ftp server is listening to");
    addProperty("mode", String.class, "bin", "FTP file mode", "Mode to transfer the file whether in binary (bin) or ascii (asc)");
  }

  protected TransferObject invoke() {
    File sourceDir = new File((String)getPropertyValue("sourcepath"));
    if (!sourceDir.exists()){
      return new TransferObject(TransferObject.ERROR, "SOURCE_PATH_DOES_NOT_EXIST");
    }
    if (!sourceDir.isDirectory()){
      return new TransferObject(TransferObject.ERROR, "SOURCE_PATH_NOT_DIRECTORY");
    }
    String pat = (String)getPropertyValue("sourcefile");
    try {
      MessageFormat fmt = new MessageFormat(pat);
      pat = fmt.format(new Object[]{getIdentifier(), getEffectiveDate()});
    } catch (IllegalArgumentException e){
      getLog().info("Pattern '" + pat + "' can not be formatted as a message, will be processed as-is");
    }
    final String pattern = pat;
    // By default, no regex at all
    Pattern regexPattern = null; //Pattern.compile(pattern, Pattern.LITERAL); <-- when we switch to J5
    try {
      regexPattern = Pattern.compile(pattern);
    } catch (PatternSyntaxException e){
      getLog().info("Interpreting pattern '" + pattern + "' literally, it is not a valid regular expression");
    }
    final Pattern finalPattern = regexPattern;
    File[] files = sourceDir.listFiles(new FileFilter(){
      public boolean accept(File pathname) {
        if (!pathname.isFile()){
          return false;
        } else if (finalPattern != null){
          Matcher matcher = finalPattern.matcher(pathname.getName());
          return matcher.matches();
        } else {
          return pattern.equalsIgnoreCase(pathname.getName());
        }
      }});
    if (files.length == 0){
      getLog().warn("No files to transmit from '" + sourceDir.getAbsolutePath() + "' with names matching '" + pattern + "'");
      return new TransferObject();
    }
    SynchronousFTPBean ftp = new SynchronousFTPBean((String)getPropertyValue("user"), (String)getPropertyValue("password"), (String)getPropertyValue("host"), getPort());
    try {
      for (int i = 0; i < files.length; i++) {
        String target = (String)getPropertyValue("targetpath");
        // If the target dir is not blank then make sure that the last
        // path element doesn't end up attached to the file name,
        // i.e. if the targetpath is /home/user it should be 
        // /home/user/f1.txt not /home/userf1.txt 
        if (target.length() > 0){
          target = Strings.ensureEndsWith(target, "/");
        }
        target = target + files[i].getName();
        String mode = (String)getPropertyValue("mode");
        getLog().info((i+1) + " of " + files.length + ": Sending '" + files[i].getAbsolutePath() + "' to target '" + target + "' using " + mode);
        ftp.uploadFileToPath(files[i], target, mode);   
      }
    } catch (IOException e) {
      getLog().error("FTP transfer failed", e);
      return new TransferObject(TransferObject.ERROR, "TRANSFER_FAILED");
    }
    
    return new TransferObject();
  }

  private int getPort() {
    try {
      return ((Integer)getPropertyValue("port")).intValue();
    } catch (NumberFormatException e){
      return 21;
    }
  }

	protected String getBatchDocumentation() {
		return "Batch program to FTP file";
	}

	protected String getBatchSummary() {
		return "This batch program runs on scheduled time and FTP-es a given file to another location";
	}
}