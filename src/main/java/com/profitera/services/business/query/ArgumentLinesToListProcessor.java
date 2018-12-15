package com.profitera.services.business.query;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ArgumentLinesToListProcessor extends BaseListQueryProcessor {
  
  private static final String SOURCE = "SOURCE_ARGUMENT";
  private static final String TARGET = "TARGET_ARGUMENT";

  public ArgumentLinesToListProcessor() {
    addRequiredProperty(SOURCE, String.class, "The source of the lines", "This argument is parsed, each line into a list element");
    addRequiredProperty(TARGET, String.class, "The list argument created", "This argument is assigned the values from the source as a list of text items");
  }

  @Override
  protected String getDocumentation() {
    return "Transforms a multi-line text argument into a list of text, each is a line from the source with any whitespace"
    + " trimmed from both sides. Any lines that are blank are excluded from the results and an empty list is always assigned to"
    + " the target argument, even if the source argument is null.";
  }

  @Override
  protected String getSummary() {
    return "Transforms a multi-line text argument into a list of text, one row per line of source text";
  }

  @Override
  public Map preprocessArguments(Map arguments, IQueryService qs)
      throws TransferObjectException {
    String target = (String) getProperty(TARGET);
    List result = new ArrayList();
    String source = (String) getProperty(SOURCE);
    Object sourceVal = arguments.get(source);
    if (sourceVal instanceof String) {
      String lines = (String) sourceVal;
      List<String> text = getLines(lines);
      for (String s : text) {
        s = s.trim();
        if (s.length() > 0) {
          result.add(s);
        }
      }

    }
    arguments.put(target, result);
    return arguments;
  }

  private List<String> getLines(String lines) {
    List<String> result = new ArrayList<String>();
    Reader r = new CharArrayReader(lines.toCharArray());
    BufferedReader br = new BufferedReader(r);
    try {
      String l  = br.readLine();
      while (l != null) {
        result.add(l);
        l  = br.readLine();
      }
    } catch (IOException e) {
      //This will never happen
    }
    return result;
  }

}
