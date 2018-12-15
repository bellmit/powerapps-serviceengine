package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.util.MapListUtil;

public class ListArgumentInjectionMergeQueryProcessor extends
		MergeQueryProcessor {

	protected String getDocumentation() {
		return "An extension to MergeQueryProcessor, the purpose of this processor is to read certain \n"
		+ "column value of the available result, put it in a list, makes sure that the list contains unique\n"
		+ "elements only (if option is enabled) and passes it as an argument to the query specified for\n"
		+ "merge query. It expects all the arguments expected by MergeQueryProcessor  and accepts additional\n"
		+ "arguments that are: MEMBERSHIP_KEY which specifies the column of result to be put in a list\n"
		+ "ARGUMENT_KEY specifies the name of the list when place into the argument map IS_ARGUMENT_UNIQUE \n"
		+ "expects a boolean input, enabling the list unique checking. False by default. MERGE_ARGUMENT \n"
		+ "expects a boolean input and determines whether, if there is a list already present at ARGUMENT_KEY\n"
		+ "whether it should be appended to (true) or overwritten (false). MAX_ARGUMENT_SIZE expects an integer\n"
		+ "and is used to limit the size of the list generated, the merge query will be re-executed for each\n "
		+ "fraction of the results in order to merge all the rows, by default it is not set which effectively \n"
		+ "makes it unlimited.\n"
		+ " "
		+ "<warning><title>MAX_ARGUMENT_SIZE</title>"
        + "<para>"
        + "The use of this argument in conjunction with a NOT IN clause will not return"
        + "the desired results, if the in clause elements are too many to retieve in"
        + "a single execution the 'not in' will not be applied to all elements."
        + "</para>"
        + "</warning>";
	}

	protected String getSummary() {
		return "Work the same like MergeQueryProcessor but with additional support";
	}

	private static final String MERGE_ARGUMENT = "MERGE_ARGUMENT";
	private static final String IS_ARGUMENT_UNIQUE = "IS_ARGUMENT_UNIQUE";
	private static final String ARGUMENT_KEY = "ARGUMENT_KEY";
	private static final String MEMBERSHIP_KEY = "MEMBERSHIP_KEY";
	private static final String MAX_ARGUMENT_SIZE = "MAX_ARGUMENT_SIZE";
	private String membershipKey;
	private String argumentKey;
	private Boolean isArgUnique = Boolean.FALSE;
	private Boolean mergeExisting;
	private int maxListSize = -1;

	public ListArgumentInjectionMergeQueryProcessor(){
		addRequiredProperty(MEMBERSHIP_KEY, String.class, "To specifies the column of result to be put in a list", "To specifies the column of result to be put in a list");
		addRequiredProperty(ARGUMENT_KEY, String.class, "To specifies the name of the list when place into the argument map", "To specifies the name of the list when place into the argument map");
		addProperty(IS_ARGUMENT_UNIQUE, String.class, Boolean.FALSE, "Enabling the list unique checking. False by default.", "Enabling the list unique checking. False by default.");
		addProperty(MERGE_ARGUMENT, String.class, Boolean.TRUE, "Determines whether, if there is a list already present at ARGUMENT_KEY \nwhether it should be appended to (true) or overwritten (false)", "Determines whether, if there is a list already present at ARGUMENT_KEY whether\n it should be appended to (true) or overwritten (false). True by default");
		addProperty(MAX_ARGUMENT_SIZE, String.class, null, "An integer and is used to limit the size of the list generated", "An integer and is used to limit the size of the list generated. By default is unlimited");
	}
	
	protected void configureProcessor() {
		super.configureProcessor();
		membershipKey = (String)getProperty(MEMBERSHIP_KEY);
		argumentKey = (String)getProperty(ARGUMENT_KEY);
		isArgUnique = new Boolean(
				(getProperty(IS_ARGUMENT_UNIQUE) != null && getProperty(
						IS_ARGUMENT_UNIQUE).toString().equalsIgnoreCase("true")));
		mergeExisting = new Boolean(
				(getProperty(MERGE_ARGUMENT) == null || getProperty(
						MERGE_ARGUMENT).toString().toUpperCase().startsWith("T")));
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
		List resultData = MapListUtil.flattenValuesForKey(result,
				membershipKey, isArgUnique.booleanValue()); // filter out duplicated membership key
		List resultDataCopy = new ArrayList(resultData);
		List argumenData = (arguments.get(argumentKey) == null ? new ArrayList()
				: (List) arguments.get(argumentKey));
		List argumentDataCopy = new ArrayList(argumenData);
		while (resultData.size() > 0) {
			List resultSubList = resultData.subList(0, Math.min(
					resultData.size(), maxListSize));
			if (mergeExisting.booleanValue()) {
				List mergedList = MapListUtil.mergeList(argumenData, resultSubList,
						isArgUnique.booleanValue());
				if (mergedList.size() > maxListSize) {
					getLog().warn("Argument "
									+ argumentKey
									+ " merged with existing list argument has size of "
									+ mergedList.size()
									+ " which is greater than "
									+ maxListSize + " for "
									+ getQueryName());
				}
				arguments.put(argumentKey, mergedList);
			} else {
				arguments.put(argumentKey, resultSubList);
			}
			super.postProcessResults(arguments, result, qs);
			int len = resultSubList.size();
			if (len == resultData.size()) {
				resultData.clear();
			} else {
				resultData = resultData.subList(len, resultData.size());
			}
		}
		if (mergeExisting.booleanValue()) {
			resultDataCopy.addAll(argumentDataCopy);
		}
		arguments.put(argumentKey, resultDataCopy);
		return result;
	}
}