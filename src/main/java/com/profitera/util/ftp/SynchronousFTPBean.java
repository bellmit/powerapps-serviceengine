package com.profitera.util.ftp;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;

public class SynchronousFTPBean {
    // Formatting choice
    public static final int UPLOAD_TARGET_FORMAT_HOST_TIME = 1;
    public static final int UPLOAD_TARGET_FORMAT_TXT_TYPE = 2;
    // Other variables
    protected int outputFormat;
    protected String localhostName;
    protected String hostToUploadTo;
    protected String user;
    protected String password;
    protected String host;
    protected int port;

    public SynchronousFTPBean(String user, String password, String host, int port) {
        // Choose the default format
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        outputFormat = UPLOAD_TARGET_FORMAT_HOST_TIME;

        // Get the local host name here, this is used for the upload signiture as well
        try {
            localhostName = InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            throw new RuntimeException("Unable to acquire hostname.");
        }

        // Check if the host is given, otherwise, assume localhost
        if (host != null) {
            hostToUploadTo = host;
        } else {
            // Default to localhost
            hostToUploadTo = localhostName;
        }
        if (port == 0) {
            // Set the port to default port
            this.port = 21;
        }
    }

    // This method allow output format change
    public void setUploadTargetFileNameFormat(int formatNumber) {
        outputFormat = formatNumber;
    }

    // This method is used to format output file name based on the selected output
    // format
    protected String formatOutputName(String fileName) {
        long timestamp = new java.util.Date().getTime();
        switch (outputFormat) {
            case UPLOAD_TARGET_FORMAT_TXT_TYPE:
                return fileName + "_" + timestamp + ".txt";
            default:
            case UPLOAD_TARGET_FORMAT_HOST_TIME:
                return localhostName + "_" + timestamp + ".xml";
        }
    }

    // Call this method to upload a file given a file object
    public void uploadFile(File aFile) throws IOException, FTPException {
        upload(new FileInputStream(aFile), this.formatOutputName(aFile.getName()));
    }
    
    public void upload(InputStream s, String remoteFileName) throws IOException, FTPException{
		FTPClient ftp = new FTPClient(host, port);
		ftp.login(user, password);
		ftp.put(s, remoteFileName);
		ftp.quit();
    }

    // Call this method to upload a file given an absolute path
    public void uploadFile(String fileAbsolutePath) throws IOException, FTPException {
        uploadFile(new File(fileAbsolutePath));
    }

    // Call this method for batch upload, take a note that if one file fail to upload,
    // This method does not continue.
    // The return value is the number of file that has been successfully uploaded.
    // If you pass in a list of 8 files, and you catch an exception
    public int uploadFile(Collection fileObjectVector) throws IOException, FTPException {
        // If this is the case, don't do a thing
        if (fileObjectVector == null) {
            return 0;
        }

        // If there is zero or only one element in it, use faster algorithm
        // So don't do it the slow way
        int numberOfFileToProcess = fileObjectVector.size();
        switch (numberOfFileToProcess) {
            case 0:
                return 0;
            case 1:

                // No need to catch the exception
                uploadFile((File) fileObjectVector.iterator().next());
                return 1;
            default:

                // Setup
                int totalFileUploadedSoFar = 0;

                // Open the FTP port here
                FTPClient ftp = new FTPClient(host, port);
                ftp.login(user, password);

                // Put the file here
                Iterator i = fileObjectVector.iterator();
                File currentFileBeingProcessed;
                try {
                    while (i.hasNext()) {
                        currentFileBeingProcessed = (File) i.next();
                        ftp.put(new FileInputStream(currentFileBeingProcessed),
                            this.formatOutputName(currentFileBeingProcessed.getName()));
                        totalFileUploadedSoFar++;
                    }
                } catch (IOException e1) {
                    // Absorb it here
                } catch (FTPException e2) {
                    // Absorb it here
                }
                // completed, logging out now
                ftp.quit();
                return totalFileUploadedSoFar;
        }
    }

    public void uploadFileToPath(File file, String targetPath, String mode) throws IOException {
      FTPClient ftp;
      try {
        ftp = new FTPClient(host, port);
        ftp.login(user, password);
        if (mode.equals("bin")){
        ftp.setType(FTPTransferType.BINARY);
        } else {
          ftp.setType(FTPTransferType.ASCII);  
        }
        ftp.put(file.getAbsolutePath(), targetPath);
        ftp.quit();
      } catch (FTPException e) {
        // Convert to an IO exception, with stack trace
        IOException exception = new IOException("Error in FTP operation: " + e.getMessage());
        exception.setStackTrace(e.getStackTrace());
        throw exception;
      }
    }
}