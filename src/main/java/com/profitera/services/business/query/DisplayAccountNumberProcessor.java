package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class DisplayAccountNumberProcessor extends BaseListQueryProcessor{

	private static final int INTERVAL = 4;
	
	public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
		if(result!=null){
			for(int i=0;i<result.size();i++){
				Map m = (Map)result.get(i);
				String displayNumber = null;
				String accountNumber = (String) m.get("ACCOUNT_NUMBER");
		    if (accountNumber != null){
		      if (accountNumber == null) return null;
		      displayNumber = accountNumber.trim();
		      final int len = displayNumber.length();
		      StringBuffer sb = new StringBuffer(len << 1); //provide space for dashes
		      int k;
		      for (k = 0; k < len - INTERVAL; k += INTERVAL) {
		          sb.append(displayNumber.substring(k, k + INTERVAL));
		          sb.append('-');
		      }
		      sb.append(displayNumber.substring(k));
		      displayNumber = sb.toString();
		    }
		    m.put("DISPLAY_ACCOUNT_NUMBER", displayNumber);
			}
		}
		
    return super.postProcessResults(arguments, result, qs);
  }

	protected String getDocumentation() {
		return "Append desh after every 4 characters to make it more readable. \n "
		+ "It will process the value from column \"ACCOUNT_NUMBER\" and store the result in \"DISPLAY_ACCOUNT_NUMBER\" \n"
		+ "eg: before: 1234567890123456 \n"
		+ "    after : 1234-5678-9012-3456";
	}

	protected String getSummary() {
		return "Replacement of DisplayAccountNumberInterceptor";
	}

}
