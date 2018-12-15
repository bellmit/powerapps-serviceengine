package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Map;

public class Section38CheckLetterTreatmentProcessManager extends LetterTreatmentProcessManager {
  public Map[] verifyProcessCreation(Map process, Long accountId, Long typeId, String user, IReadOnlyDataProvider provider) throws TreatmentProcessCreationException {
    Map[] temp = super.verifyProcessCreation(process, accountId, typeId, user, provider);
    for (int i = 0; i < temp.length; i++) {
      try {
        Map obj = (Map) provider.queryObject("getIsNewSection38Permitted", temp[i]);
        Boolean perm = obj == null ? null : (Boolean) obj.get("IS_PERMITTED");
        if (perm != null && perm.booleanValue() == false){
          throw new TreatmentProcessCreationException(
          "Unable to save treatment action. Section 38 letter was generated previously.");  
        }
      } catch (SQLException e) {
        throw new TreatmentProcessCreationException("Unable to execute section 38 letter check");
      }
    }
    return temp;  
  }
}
