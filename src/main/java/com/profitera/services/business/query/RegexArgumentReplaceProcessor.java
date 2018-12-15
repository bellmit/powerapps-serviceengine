package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class RegexArgumentReplaceProcessor extends BaseListQueryProcessor {
  private static final String ARGUMENT = "ARGUMENT";
  private static final String REPLACEMENT = "REPLACEMENT";
  private static final String REGEX = "REGEX";

  public RegexArgumentReplaceProcessor(){
    addRequiredProperty(ARGUMENT, String.class, "The argument to change", "The name of the query argument that the regex should be applied to.");
    addRequiredProperty(REGEX, String.class, "The regular expression", "The regular expression that will be used to capture a protion of the argument value for replacement.");
    addProperty(REPLACEMENT, String.class, "", "The text to replace the match", "This text is used to replace the text matched by the regular expression, it is a literal string not a regular expression.");
  }

  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    Object v = arguments.get(getProperty(ARGUMENT));
    if (v != null){
      if (v instanceof Collection){
        List l = new ArrayList((Collection)v);
        for (int i = 0; i < l.size(); i++) {
          l.set(i, replaceAll(l.get(i)));
        }
        v = l;
      } else {
        v = replaceAll(v);
      }
      arguments.put(getProperty(ARGUMENT), v);
    }
    return super.preprocessArguments(arguments, qs);
  }

  private Object replaceAll(Object v) {
    if (v == null){
      return null;
    } else {
      return v.toString().replaceAll(getProperty(REGEX).toString(), getProperty(REPLACEMENT).toString());
    }
  }

  protected String getDocumentation() {
    return "Uses the regular expression defined in " + REGEX + " to match the value in the argument defined by " + ARGUMENT + " with the string defined by " + REPLACEMENT + "."
    + " Null values are passed through untouched and non-string values are turned to strings using the Java toString() method. An invalid regular expression will cause execution to fail.";
  }

  protected String getSummary() {
    return "Replaces the portion of the argument value that matches the regular expression with the specified value";
  }

}
