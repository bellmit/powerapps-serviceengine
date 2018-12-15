package com.profitera.services.business.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.client.RemoteProgressThread;
import com.profitera.deployment.rmi.ApplicationServerServiceIntf;
import com.profitera.deployment.rmi.DocumentDownloadServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.appserver.impl.AppServerService;
import com.profitera.services.system.document.IArchivePathResolver;
import com.profitera.services.system.document.IDocumentHeader;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.document.impl.ArchivePathResolver;
import com.profitera.services.system.lookup.IRemoteRunnable;
import com.profitera.services.system.lookup.IRemoteServiceRequest;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.lookup.RemoteConnectionException;
import com.profitera.services.system.lookup.RemoteServiceRequestFactory;
import com.profitera.util.MapListUtil;

public class DocumentDownloadService extends ProviderDrivenService implements DocumentDownloadServiceIntf {
  private static final String DOWNLOAD_SQL_ERROR = "DOWNLOAD_SQL_ERROR";
  private static final String DOWNLOAD_IO_ERROR = "DOWNLOAD_IO_ERROR";
  private SecureRandom random = new SecureRandom();
  private Map currentDownloads = new HashMap();
  
  private class DownloadStruct {
    InputStream input;
    long startTime;
    DownloadStruct(InputStream i, long start){
      input = i;
      startTime = start;
    }
  }
  
  public TransferObject startDocumentDownload(Long docId){
  	TransferObject to = getDocumentContent(docId);
  	if(to.isFailed()) return to;
  	byte[] documentContent = (byte[])to.getBeanHolder();
  	assert(documentContent!=null);
  	Long id = new Long(random.nextLong());
  	while(currentDownloads.containsKey(id)){
  		id = new Long(random.nextLong());
  	} // this is not likely to happen but we never know
  	try {
  		currentDownloads.put(id, createStruct(documentContent));
      return new TransferObject(id);
    } catch (IOException e) {
      log.error("Unable to read download buffer for transfer", e);
      return new TransferObject(TransferObject.ERROR, DOWNLOAD_IO_ERROR);
    }
  }
  
  private TransferObject getDocumentContent(Long docId) {
		if (retrieveFromSlave(docId)) {
			return downloadDocumentFromSlave(docId, getSlaveServer());
		}else{
			return downloadDocumentFromLocal(docId);
		}
  }
  
  protected TransferObject downloadDocumentFromSlave(final Long docId, List slave){
  	IRemoteRunnable runnable = new IRemoteRunnable(){
  		public TransferObject run(Object serviceInstance) {
  			DocumentDownloadServiceIntf service = (DocumentDownloadServiceIntf)serviceInstance;
  			return service.downloadDocumentFromLocal(docId);
  		}
  	};
  	return executeService(docId, runnable, slave);
  }
  
  public TransferObject downloadDocumentFromLocal(Long docId){
  	IDocumentService docService = getDocumentService();
		try {
			byte[] content = docService.getDocumentContent(docId, getReadWriteProvider());
			if (content == null) {
				return new TransferObject(TransferObject.ERROR, "NO_SUCH_DOCUMENT");
			}
			return new TransferObject(content);
		} catch (SQLException e) {
			log.error("Database retrieval failed for document", e);
			return new TransferObject(TransferObject.ERROR, DOWNLOAD_SQL_ERROR);
		} catch (IOException e) {
			log.error("Unable to read download buffer for transfer", e);
			return new TransferObject(TransferObject.ERROR, DOWNLOAD_IO_ERROR);
		}
  }
  
  private DownloadStruct createStruct(byte[] documentContent) throws IOException{
  	 File temp = File.createTempFile("temp", ".dds");
     temp.deleteOnExit();
     OutputStream os = new FileOutputStream(temp);
     os.write(documentContent);
     os.close();
     DownloadStruct ds = new DownloadStruct(new FileInputStream(temp), System.currentTimeMillis());
     return ds;
  }
  
  public TransferObject downloadDocumentSegment(Long downloadId){
    DownloadStruct s = (DownloadStruct) currentDownloads.get(downloadId);
    if (s == null) return new TransferObject(TransferObject.ERROR, "NO_SUCH_DOWNLOAD");
    if (s.input == null) return new TransferObject();
    byte[] bytes = new byte[getBufferSize()];
    try {
      int read = s.input.read(bytes);
      if (read < bytes.length){
        if (read == -1){
          bytes = null;
        } else {
          byte[] temp = new byte[read];
          System.arraycopy(bytes, 0, temp, 0, read);
          bytes = temp;
        }
        s.input.close();
        s.input = null;
      }
      return new TransferObject(bytes);
    } catch (IOException e) {
      log.error("Unable to read download buffer for transfer", e);
      return new TransferObject(TransferObject.ERROR, "DOWNLOAD_IO_ERROR");
    }
  }
  
  public TransferObject getDocumentFragments(Long docId, long startId,
      int rowLimit) {
  	if(retrieveFromSlave(docId)){
  		return getDocumentFragmentsFromSlave(docId, startId, rowLimit, getSlaveServer());
  	}else{
  		return getDocumentFragmentsFromLocal(docId, startId, rowLimit);
  	}
  }
  
  public TransferObject getDocumentFragmentsFromLocal(Long docId, long startId,
      int rowLimit) {
    if (rowLimit == 0) {
      rowLimit = 100;
    }
    IDocumentService documentService = getDocumentService();
    try {
      String[] documentLines = documentService.getDocumentLines(docId, startId, rowLimit);
      List l = new ArrayList();
      if (startId == 0) {
        l.add(documentService.getDocument(docId, getReadWriteProvider()).isEncoded());
      }
      l.addAll(Arrays.asList(documentLines));
      return new TransferObject(l);
    } catch (SQLException e) {
      log.error("Database retrieval failed for document", e);
      return new TransferObject(TransferObject.ERROR, DOWNLOAD_SQL_ERROR);
    } catch (IOException e) {
      log.error("Database retrieval failed for document", e);
      return new TransferObject(TransferObject.ERROR, DOWNLOAD_IO_ERROR);
    }
  }
  
  protected TransferObject getDocumentFragmentsFromSlave(final Long documentId, final long start, final int count, List slave) {
  	IRemoteRunnable runnable = new IRemoteRunnable(){
  		public TransferObject run(Object serviceInstance) {
  			DocumentDownloadServiceIntf service = (DocumentDownloadServiceIntf)serviceInstance;
  			return service.getDocumentFragmentsFromLocal(documentId, start, count);
  		}
  	};
  	return executeService(documentId, runnable, slave);
  }
  
  private TransferObject executeService(Long documentId, IRemoteRunnable runnable, List slave){
  	for(int i=0;i<slave.size();i++){
  		Map server = (Map)slave.get(i);
  		/*if(server.get(ApplicationServerServiceIntf.ACTIVE_SERVER).equals("N")){
  			continue;
  		}*/
  		List addressList = AppServerService.getServerConfiguredAddresses(server);
  		for(int j = 0; j<addressList.size();j++){
  			String address = (String)addressList.get(j);
  			try{
      		IRemoteServiceRequest request = getRemoteRequest(address);
      		TransferObject to = request.execute(runnable);
      		if(!to.isFailed()){
      			log.info("Successed to retrieve archived document "+documentId+" from "+address);
      			return to;
      		}
  			}catch(MalformedURLException e){
      		log.error("Failed to retrieve archvied document "+documentId+" from "+address, e);
      	}catch(RemoteConnectionException e){
      		log.error("Failed to retrieve archvied document "+documentId+" from "+address, e);
      	}
  		}
  	}	
  	return new TransferObject(TransferObject.ERROR, "DOCUMENT_NOT_FOUND");
  }
  
  protected boolean retrieveFromSlave(Long docId){
  	try{
  		IDocumentHeader header = getDocumentHeader(docId);
  		if(header.isArchived()){
  			String archiveDirectory = ServiceEngine.getProp(IDocumentService.DOCUMENT_SERVICE_ARCHIVE_DIR);
  	  	if(archiveDirectory==null){
  	  		// is archived but might be archived at slave
  	  		return true;
  	  	}
  			IArchivePathResolver resolver = new ArchivePathResolver(archiveDirectory, ".arc");
  			File archviedFile = resolver.getPath(docId);
  			if(!archviedFile.exists()) return true;
   		}
  	}catch(SQLException e){
  		log.error("Failed to retrieve document archive info", e);
  	}
  	return false;
  }
  
  protected List getSlaveServer(){
  	ApplicationServerServiceIntf appService = getApplicationServerService();
  	if(appService==null) {
  		log.error(RemoteProgressThread.APPLICATION_SERVER_SERVICE + " is not running.");
  		return Collections.EMPTY_LIST;
  	}
  	TransferObject to = appService.getServers();
  	if(to.isFailed()){
  		log.error("Failed to retrieve server information.");
  		return Collections.EMPTY_LIST;
  	}
  	List servers = (List)to.getBeanHolder();
  	TransferObject c = appService.getCurrentServerId();
  	if(c.isFailed()){
  		log.error("Failed to retrieve server information.");
  		return Collections.EMPTY_LIST;
  	}
  	Long currentServer = (Long)c.getBeanHolder();
  	int index = MapListUtil.firstIndexOf(ApplicationServerServiceIntf.ID, currentServer, servers);
  	servers.remove(index);
  	return servers;
  }
  
  protected int getBufferSize() {
    return 1024 * 512; // 1/2MB
  }
  
  private IDocumentHeader getDocumentHeader(Long docId) throws SQLException{
  	IDocumentService documentService = getDocumentService();
  	return documentService.getDocument(docId, getReadWriteProvider());
  }
  
  protected IRemoteServiceRequest getRemoteRequest(String serverAddress) throws MalformedURLException{
  	RemoteServiceRequestFactory factory = new RemoteServiceRequestFactory(null, ServiceEngine.getConfig(false));
  	return factory.getRequest(serverAddress, RemoteProgressThread.DOCUMENT_DOWNLOAD_SERVICE);
  }
  
  private IDocumentService getDocumentService() {
    final IDocumentService docService = (IDocumentService) LookupManager
        .getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
    return docService;
  }  
  
  private ApplicationServerServiceIntf getApplicationServerService(){
  	return (ApplicationServerServiceIntf) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, RemoteProgressThread.APPLICATION_SERVER_SERVICE);
  }

  @Override
  public TransferObject getTotalDocumentFragments(Long documentId) {
    if(retrieveFromSlave(documentId)){
      return new TransferObject(-1L);
    }else{
      try {
        return new TransferObject(getDocumentService().getDocumentLineCount(documentId));
      } catch (SQLException e) {
        log.error("Database retrieval failed for document", e);
        return new TransferObject(TransferObject.EXCEPTION, DOWNLOAD_SQL_ERROR);
      }
    }
  }

}
