package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public class ListArgumentMergeQueryProcessor extends MergeQueryProcessor {

	protected String getDocumentation() {
		return "LQP to use when a merge is known to use a list argument and an &#34;in&#34; clause";
	}

	protected String getSummary() {
		return "This LQP is used when a merge is known to use a list argument and an &#34;in&#34; clause,\n"
		+ "you can limit the size of the in clause and re-execute the query for each subset of the original\n"
		+ "argument.";
	}

	private static final String ARGUMENT_KEY = "ARGUMENT_KEY";
	private static final String MAX_ARGUMENT_SIZE = "MAX_ARGUMENT_SIZE";
	private String argumentKey;
	private int maxListSize = -1;

	public ListArgumentMergeQueryProcessor(){
		addRequiredProperty(ARGUMENT_KEY, String.class, "To specifies the column of result to be put in a list", "To specifies the column of result to be put in a list");
		addProperty(MAX_ARGUMENT_SIZE, String.class, null, "An integer and is used to limit the size of the list generated", "An integer and is used to limit the size of the list generated. By default is unlimited");
	}
	
	protected void configureProcessor() {
		super.configureProcessor();
		argumentKey = (String)getProperty(ARGUMENT_KEY);
		String max = (String)getProperty(MAX_ARGUMENT_SIZE);
		if (max != null) {
			try {
				maxListSize = Integer.parseInt(max);
			} catch (NumberFormatException e) {
				throw new NumberFormatException(getQueryName() + " processor "
						+ getClass().getName() + " " + MAX_ARGUMENT_SIZE
						+ " invalid: " + max);
			}
		}
		if (maxListSize <= 0) {
			maxListSize = Integer.MAX_VALUE;
		}
	}

	public List postProcessResults(Map arguments, List result, IQueryService qs)
			throws TransferObjectException {
		if (result == null || result.size() == 0)
			return result;
		List argumentData = (List) arguments.get(argumentKey);
		List argumentDataCopy = new ArrayList(argumentData);
		while (argumentData.size() > 0) {
			List listArg = argumentData.subList(0, Math.min(
					argumentData.size(), maxListSize));
			arguments.put(argumentKey, listArg);
			super.postProcessResults(arguments, result, qs);
			int len = listArg.size();
			if (len == argumentData.size()) {
				argumentData.clear();
			} else {
				argumentData = argumentData.subList(len, argumentData.size());
			}
		}
		arguments.put(argumentKey, argumentDataCopy);
		return result;
	}
}