package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.MapListUtil;

public class MembershipArgumentQueryProcessor extends
    AbstractListQueryProcessor {

  private String argName;
  private String membershipKey;
  private String membershipQueryName;
  private Boolean mergeExisting = Boolean.FALSE;
  private Boolean isArgUnique = Boolean.FALSE;

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    TransferObject to = qs.getQueryList(membershipQueryName, arguments);
    if (to.isFailed()){
      throw new TransferObjectException(to);
    }
    List result = (List) to.getBeanHolder();
    result = MapListUtil.flattenValuesForKey(result, membershipKey);
    if (arguments == null)
      arguments = new HashMap();
    List finalResult = (!mergeExisting.booleanValue() || arguments.get(argName) == null ? new ArrayList() : (List)arguments.get(argName));
    finalResult = MapListUtil.mergeList(finalResult, result, isArgUnique.booleanValue());
    arguments.put(argName, finalResult);
    return arguments;
  }

  protected void configureProcessor() {
    super.configureProcessor();
    argName = getRequiredProperty("ARGUMENT_KEY");
    membershipQueryName = getRequiredProperty("MEMBERSHIP_QUERY");
    membershipKey = getRequiredProperty("MEMBERSHIP_KEY");
		isArgUnique = new Boolean((getProperty("IS_ARGUMENT_UNIQUE") != null && getProperty("IS_ARGUMENT_UNIQUE").equalsIgnoreCase("true"))); 
		mergeExisting = new Boolean((getProperty("MERGE_ARGUMENT") != null && getProperty("MERGE_ARGUMENT").equalsIgnoreCase("true"))); 
  }
}