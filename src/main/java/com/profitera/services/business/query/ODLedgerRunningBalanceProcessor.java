package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.DoubleUtil;
import com.profitera.util.MapListUtil;


public class ODLedgerRunningBalanceProcessor extends BaseListQueryProcessor{
	
	private static final String CLOSING_BALANCE = "CLOSING_BALANCE";
	private static final String DEBIT = "DEBIT";
	private static final String CREDIT = "CREDIT";
	private static final String RUNNING_BALANCE = "RUNNING_BALANCE";	
	private static final String SORT = "SORT";
	private static final Double ZERO = new Double(0);
	
	private String firstField;
	private String[] sort;
	private String debitKey;
	private String creditKey;
	
	public ODLedgerRunningBalanceProcessor(){
		addRequiredProperty(SORT, String.class, "The sorting fields", "A semi-colon delimited list of fields that is used to first sort the transactions, i.e. the fields that dictate the transaction order.");
		addRequiredProperty(DEBIT, String.class, "The debit amount field", "The debit amount field name.");
    addRequiredProperty(CREDIT, String.class, "The credit amount field", "The credit amount field name.");
    addRequiredProperty(RUNNING_BALANCE, String.class, "The key to assign the balance to", "The result of each row is assigned to the key.");
	  addRequiredProperty(CLOSING_BALANCE, String.class, "The balance to be used as a starting point", "The closing balance is extracted using this key from the first row of the results.");
	}
	
	protected void configureProcessor() {
		String inputString = (String)getProperty(SORT);
		String patternString = ";";
		sort = inputString.split(patternString);
		firstField = sort[0];
	}
		
	public List postProcessResults(Map arguments, List result, IQueryService qs)
	throws TransferObjectException{
	  List originalResult = result;
	  if (result.size() == 0){
	    return originalResult;
	  }
		//sort table
		result = getSortedResult(result);
		//get closing balance		
		Map firstEntry = (Map)result.get(0);
    Double bal = null;
    try {
      bal = getDouble(firstEntry, (String) getProperty(CLOSING_BALANCE), ZERO);
    } catch (ClassCastException e){
      throw new ClassCastException(getNotDoubleMessage((String) getProperty(CLOSING_BALANCE), firstEntry));
    }
		int cbal = DoubleUtil.asCents(bal);
		debitKey = (String)getProperty(DEBIT);
		creditKey = (String)getProperty(CREDIT);
		int rbal = cbal;
		//get RunningBalance
		if(isDesc()){
			for(int i=0;i<result.size();i++){
				Map row=(Map)result.get(i);
				row.put(getProperty(RUNNING_BALANCE), DoubleUtil.centsAsDouble(rbal));
				rbal = getRunningBalance(rbal,row);	
			}
		}else{
			for(int i=result.size();i>0;i--){
				Map row=(Map)result.get(i-1);
				row.put(getProperty(RUNNING_BALANCE), DoubleUtil.centsAsDouble(rbal));
				rbal = getRunningBalance(rbal,row);	
			}
		}
		originalResult.clear();
		originalResult.addAll(result);
		return originalResult;
	}

  private Double getDouble(Map firstEntry, String k, Double defaultValue) {
    Double bal = (Double)firstEntry.get(k);
    if (bal == null) return defaultValue;
    return bal;
  }

	private List getSortedResult(List result){
		boolean order = isDesc() ? false:true ;
		for(int i=0;i<sort.length;i++){
			sort[i].trim();
			if(gotFieldPrefix(sort[i])){
				sort[i]=sort[i].substring(1);
			}
		}
		result = MapListUtil.sortBy(sort,order,result);
		return result;
	}
	
	private int getRunningBalance(int rbal, Map row){
		Double d = getDebitValue(row);
		Double c = getCreditValue(row);
		int value = 0;
		if(d!=null){
			value = value - DoubleUtil.asCents(d);
		}
		if(c!=null){
			value = value + DoubleUtil.asCents(c);	
		}
		rbal = rbal + value;
		return rbal;
	}

  private Double getCreditValue(Map row) {
    Double c = null;
		try {
		  c = (Double) row.get(creditKey);
		} catch (ClassCastException e){
      throw new ClassCastException(getNotDoubleMessage(creditKey, row));
    }
    return c;
  }

  private Double getDebitValue(Map row) {
    Double d = null;
		try {
		  d = (Double) row.get(debitKey);
		} catch (ClassCastException e){
		  throw new ClassCastException(getNotDoubleMessage(debitKey, row));
		}
    return d;
  }

  private String getNotDoubleMessage(String key, Map row) {
    return key + " value expected as type double but provided as " + row.get(key).getClass().getName() + " for " + getQueryName() + " processor " + this.getClass().getName();
  }
	
	private boolean gotFieldPrefix(String field){
		return (field.startsWith("+")|field.startsWith("-"));
	}
	
	private boolean isDesc(){
		return firstField.startsWith("-");
	}

  protected String getDocumentation() {
    return "TODO: Put specs from change request here";
  }

  protected String getSummary() {
    return "Updates the running balance values for SIBS overdraft accounts";
  }
}




