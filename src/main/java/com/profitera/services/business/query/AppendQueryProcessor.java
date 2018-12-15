package com.profitera.services.business.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class AppendQueryProcessor extends BaseListQueryProcessor {
  String appendingQuery;
  public AppendQueryProcessor() {
    addRequiredProperty("APPENDING_QUERY", String.class, "Query to execute", "The secondary query that is executed to append results to the original result.");
  }
  
  @Override
  protected void configureProcessor() {
    super.configureProcessor();
    appendingQuery = (String) getProperty("APPENDING_QUERY");
  }
  
  @Override
  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    TransferObject t = qs.getQueryList(appendingQuery, arguments);
    if (t.isFailed()) {
      throw new TransferObjectException(t);
    } else {
      result.addAll((Collection) t.getBeanHolder());
    }
    return result;
  }

  @Override
  protected String getDocumentation() {
    return "Appends the results of the configured query much like an SQL UNION ALL would do. " +
    		"The configured query is offered the same arguments as the main query the processor is attached to.";
  }

  @Override
  protected String getSummary() {
    return "Appends the result of the configured query to the existing results";
  }

}
