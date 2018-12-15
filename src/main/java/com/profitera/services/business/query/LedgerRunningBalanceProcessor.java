package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.DoubleUtil;
import com.profitera.util.MapListUtil;

public class LedgerRunningBalanceProcessor 
extends BaseListQueryProcessor{
	
	private static final Pattern CREDIT_AFFECT_CODE_PATTERN = Pattern.compile("[PBCHLKOMI].*");
	private static final Pattern DEBIT_AFFECT_CODE_PATTERN = Pattern.compile("[PBCHLKOMI].*");
	private static final String DEBIT_AMOUNT="DEBIT_AMOUNT";
	private static final String CREDIT_AMOUNT="CREDIT_AMOUNT";
	private static final String AFFECTS_CODE="AFFECTS_CODE";
	private static final String TRANSACTION_TYPE="TRANSACTION_TYPE";
	private static final String PRODUCT_TYPE_CATEGORY_CODE="PRODUCT_TYPE_CATEGORY_CODE";
	private static final String PAYMENT_BEHAVIOUR_CODE="PAYMENT_BEHAVIOUR_CODE";
	private static final String OS_BALANCE="OS_BALANCE";
	private static final String SORT="SORT";
	
	private String debitAmount ;
	private String creditAmount ;
	private String outStandingBalance;
	private String affectsCode ;
	private String transactionType ;
	private String productTypeCategoryCode ;
	private String paymentBehaviourCode ;
	private Object[] key;
	private boolean[] order;
		
	public LedgerRunningBalanceProcessor(){
		addRequiredProperty(DEBIT_AMOUNT, String.class, "The debit amount field", "The debit amount field name.");
		addRequiredProperty(CREDIT_AMOUNT, String.class, "The credit amount field", "The credit amount field name.");
		addRequiredProperty(OS_BALANCE,String.class, "The outstanding balance field", "The outstanding balance field name.");
		addRequiredProperty(AFFECTS_CODE, String.class, "The affects code field", "The affects code field name.");
		addRequiredProperty(TRANSACTION_TYPE, String.class, "The transaction type field", "The transaction type field name.");
		addRequiredProperty(PRODUCT_TYPE_CATEGORY_CODE, String.class, "The product type category field", "The product type category field name.");
		addRequiredProperty(PAYMENT_BEHAVIOUR_CODE, String.class, "The payment behaviour field", "The payment behaviour field name.");
		addRequiredProperty(SORT,String.class, "The sorting fields", "A semi-colon delimited list of fields that is used to first sort the transactions, i.e. the fields that dictate the transaction order.");
	}
	
	protected void configureProcessor() {
		String inputString = (String)getProperty(SORT);
		String patternString = ";";
		String[] sort = inputString.split(patternString);		
		key = new Object[sort.length];
		order = new boolean[sort.length];
		for(int i=0;i<sort.length;i++){
			if(gotFieldPrefix(sort[i])){
				order[i] = isDesc(sort[i]) ? false : true ;
				key[i] = sort[i].substring(1);
			}else{
				order[i] = true;
				key[i] = sort[i];
			}
		}
		
		debitAmount = (String)getProperty(DEBIT_AMOUNT);
		creditAmount = (String)getProperty(CREDIT_AMOUNT);
		outStandingBalance = (String)getProperty(OS_BALANCE);
		affectsCode = (String)getProperty(AFFECTS_CODE);
		transactionType = (String)getProperty(TRANSACTION_TYPE);
		productTypeCategoryCode = (String)getProperty(PRODUCT_TYPE_CATEGORY_CODE);
		paymentBehaviourCode = (String) getProperty(PAYMENT_BEHAVIOUR_CODE);
	}
	
	public List postProcessResults(Map arguments, List result, IQueryService qs)
	throws TransferObjectException{
		//sort table
		result = MapListUtil.sortBy(key, order, result);
		//get running balance
		int rbal = 0; 
		if(order[0] == false){
			for(int i=result.size();i>0;i--){
				Map row=(Map)result.get(i-1);
				rbal = getRunningBalance(rbal,row);
				row.put(outStandingBalance, DoubleUtil.centsAsDouble(rbal));
			}
		}else{
			for(int i=0;i<result.size();i++){
				Map row=(Map)result.get(i);
				rbal = getRunningBalance(rbal,row);	
				row.put(outStandingBalance, DoubleUtil.centsAsDouble(rbal));
			}
		}
		
		return super.postProcessResults(arguments, result, qs);
	}
	
	private int getRunningBalance(int rbal, Map row){
		  int amount = 0;
		  if(isRunningBalanceTrans(row)){
			  amount = getTransactionAmount(row);
			  rbal = rbal + amount;
		  }
		  return rbal;
	  }
	
	private boolean isRunningBalanceTrans(Map row) {
	    boolean isDebit = isDebit(row);
	    String affects = (String) row.get(affectsCode);
	    String code = (String) row.get(transactionType);
	    if (isDebit){
	    	return isDebitRunningBalanceTransaction(affects, code);
	    }else{
	    	String pc = (String) row.get(productTypeCategoryCode);
	    	String payBehaviour = (String) row.get(paymentBehaviourCode);
	    	return isCreditRunningBalanceTransaction(pc, payBehaviour, affects, code);
	    }
	}
	
	private int getTransactionAmount(Map row) {
	    int creditDebit = isDebit(row) ? 1 : -1;
	    Double amount = (Double) row.get(debitAmount);
	    if (amount == null){
	    	amount = (Double) row.get(creditAmount);
	    }
	    if (amount == null){
	    	return 0;
	    }
	    int newAmount = DoubleUtil.asCents(amount);
	    newAmount = newAmount * creditDebit;
	    return newAmount;
	}
	    
	
	/**
	   * If DEBIT_CREDIT_FLAG + AFFECTS_CODE equals to:
	   * CB
	   * CC
	   * CH
	   * CL
	   * CK
	   * CO
	   * CM
	   * CP
	   * CI
	   * C
	   * OR transaction code 301 (which indicates Accrued Interest)
	   *     AND PRODUCT_TYPE_CATEGORY_CODE=?RC? (Revolving Credit)
	   *     AND PAYMENT_BEHAVIOUR_ID=?5? (Single Pay)
	   * OR transaction code 497 (which indicates billed interest credited)
	   * @param affects
	   * @param code
	   * @return
	   */
	  private boolean isCreditRunningBalanceTransaction(String prodCat, String paymentBehaviour, String affects, String code) {
		  if (affects == null || affects.length() == 0){ // The covers 'C' (no affects code)
			  return true;
		  }
		  if (code != null){
			  if (code.equals("497")){
				  return true;
			  }
			  if (paymentBehaviour != null && prodCat != null && code.equals("301") && paymentBehaviour.equals("5") && prodCat.equals("RC")){
				  return true;
			  }
		  }
		  return CREDIT_AFFECT_CODE_PATTERN.matcher(affects).find();
	  }

	  /**
	   * If DEBIT_CREDIT_FLAG + AFFECTS_CODE equals to:
	   * DP
	   * DB
	   * DC
	   * DH
	   * DL
	   * DK
	   * DO
	   * DM
	   * DI
	   * D
	   * DN AND TRANSACTION CODE = 496 (which indicates billed interest debited)
	   * THEN 
	   * Add PTRTRANSACTION.AMOUNT to the balance
	   * @param affects
	   * @param code
	   * @return
	   */
	  private boolean isDebitRunningBalanceTransaction(String affects, String code) {
		  if (affects == null || affects.length() == 0){ // The covers 'D' (no affects code)
			  return true;
		  }
		  if (affects.startsWith("N") && code!=null && code.equals("496")){ // Special DN case
			  return true;
		  }
		  return DEBIT_AFFECT_CODE_PATTERN.matcher(affects).find();
	  }
	  
	  private boolean isDebit(Map row){
		  return row.get((String)getProperty(DEBIT_AMOUNT))!=null;
	  }
	 
	  private boolean isDesc(String field){
		  return field.startsWith("-");
	  }
	  	  
	  private boolean gotFieldPrefix(String field){
		  return (field.startsWith("+")|field.startsWith("-"));
	  }

    protected String getDocumentation() {
      return "TODO: Find CR specification and put here.";
    }

    protected String getSummary() {
      return "Calculates the running balance of a term loan in SIBS based on the transaction history";
    }  
	  
}