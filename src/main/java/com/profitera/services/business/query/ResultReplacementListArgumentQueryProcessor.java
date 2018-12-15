package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ResultReplacementListArgumentQueryProcessor extends AbstractListQueryProcessor {

	private static final String MAX_ARGUMENT_SIZE = "MAX_ARGUMENT_SIZE";
	private String argumentKey;
	private Boolean isArgUnique = Boolean.FALSE;
  private int maxListSize = -1;
  private String replacementQueryName;


	protected void configureProcessor() {
		super.configureProcessor();
    replacementQueryName = getRequiredProperty("REPLACEMENT_QUERY");
		argumentKey = getRequiredProperty("ARGUMENT_KEY");
		isArgUnique = new Boolean((getProperty("IS_ARGUMENT_UNIQUE") != null && getProperty("IS_ARGUMENT_UNIQUE").equalsIgnoreCase("true")));
    String max = getProperty(MAX_ARGUMENT_SIZE);
    if (max != null){
      try {
      maxListSize = Integer.parseInt(max);
      } catch (NumberFormatException e){
        throw new NumberFormatException(getQueryName() + " processor " + getClass().getName() + " " + MAX_ARGUMENT_SIZE + " invalid: " + max);
      }
    }
    if (maxListSize <=0){
      maxListSize = Integer.MAX_VALUE;
    }
	}

	public List postProcessResults(Map arguments, List result, IQueryService qs)
			throws TransferObjectException {
    getLog().debug("Discarding " + result.size() + "result elements for " + getQueryName());
		List argumentData = (List) arguments.get(argumentKey);
    if (argumentData == null || argumentData.size() == 0){
      return new ArrayList();
    }
    if (isArgUnique.booleanValue()){
      Set s = new HashSet(argumentData);
      argumentData.clear();
      argumentData.addAll(s);
      
    }
    result = new ArrayList(argumentData.size());
    List argumentDataCopy = new ArrayList(argumentData);
    while (argumentData.size() > 0){
      List listArg = argumentData.subList(0, Math.min(argumentData.size(), maxListSize));
      arguments.put(argumentKey, listArg);
      result.addAll((Collection) qs.getQueryList(replacementQueryName, arguments).getBeanHolder());
      super.postProcessResults(arguments, result, qs);
      int len = listArg.size();
      if (len == argumentData.size()){
        argumentData.clear();
      } else {
        argumentData = argumentData.subList(len, argumentData.size());
      }
    }
    arguments.put(argumentKey, argumentDataCopy);
    return result;
	}

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }
}