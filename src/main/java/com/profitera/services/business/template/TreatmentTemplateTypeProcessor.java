package com.profitera.services.business.template;

import java.util.HashMap;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.business.query.AbstractListQueryProcessor;
import com.profitera.services.business.query.IQueryService;
import com.profitera.services.system.dataaccess.RPMDataManager;

public class TreatmentTemplateTypeProcessor extends AbstractListQueryProcessor {
  public static int LETTER = 61001;
  //public static int EMAIL = 61002;	(re: Bug 1015)
  //public static int FAX = 61003;		(re: Bug 1015)		
  public static int SMS = 61004;
  public static int NOTE = 61005;
  public static int BUSINESS_FORM = 61006;
  //
  private Map typeMapping;
  private String processTypeKey;
  private String templateTypeKey;

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
	  Long id ;
	  if(arguments.get(processTypeKey) instanceof Long)
		  id = (Long) arguments.get(processTypeKey);
	  else
		  id = new Long(String.valueOf(arguments.get(processTypeKey)));
    if (id != null){
      arguments.put(templateTypeKey, typeMapping.get(id));
    }
    return arguments;
  }

  protected void configureProcessor() {
    super.configureProcessor();
    processTypeKey = getRequiredProperty("SELECTION_KEY");
    templateTypeKey = getRequiredProperty("TEMPLATE_TYPE_KEY");
    typeMapping = new HashMap();
    typeMapping.put(new Long(RPMDataManager.LETTER_TREATMENT_PROCESS), new Long(LETTER));
    // Appointments are allowed business forms
    typeMapping.put(new Long(RPMDataManager.APPOINTMENT_TREATMENT_PROCESS), new Long(BUSINESS_FORM));
    typeMapping.put(new Long(RPMDataManager.SMS_TREATMENT_PROCESS), new Long(SMS));
    typeMapping.put(new Long(RPMDataManager.NOTES_TREATMENT_PROCESS), new Long(NOTE));
    typeMapping.put(new Long(RPMDataManager.TPS_TREATMENT_PROCESS), new Long(BUSINESS_FORM));
  }

}
