package org.mifosng.platform.api.infrastructure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;

public class ApiParameterHelper {

	public static Set<String> extractFieldsForResponseIfProvided(final MultivaluedMap<String, String> queryParams) {
		Set<String> fields = new HashSet<String>();
		String commaSerperatedParameters = "";
		if (queryParams.getFirst("fields") != null) {
			commaSerperatedParameters = queryParams.getFirst("fields");
			if (StringUtils.isNotBlank(commaSerperatedParameters)) {
				fields = new HashSet<String>(Arrays.asList(commaSerperatedParameters.split("\\s*,\\s*")));
			}
		}
		return fields;
	}
	
	public static Set<String> extractAssociationsForResponseIfProvided(final MultivaluedMap<String, String> queryParams) {
		Set<String> fields = new HashSet<String>();
		String commaSerperatedParameters = "";
		if (queryParams.getFirst("associations") != null) {
			commaSerperatedParameters = queryParams.getFirst("associations");
			if (StringUtils.isNotBlank(commaSerperatedParameters)) {
				fields = new HashSet<String>(Arrays.asList(commaSerperatedParameters.split("\\s*,\\s*")));
			}
		}
		return fields;
	}
	
	public static boolean prettyPrint(final MultivaluedMap<String, String> queryParams) {
		boolean prettyPrint = false;
		if (queryParams.getFirst("pretty") != null) {
			String prettyPrintValue = queryParams.getFirst("pretty");
			prettyPrint = "true".equalsIgnoreCase(prettyPrintValue);
		}
		return prettyPrint;
	}
	
	public static boolean exportCsv(final MultivaluedMap<String, String> queryParams) {
		boolean exportCsv = false;
		if (queryParams.getFirst("exportCSV") != null) {
			String exportCsvValue = queryParams.getFirst("exportCSV");
			exportCsv = "true".equalsIgnoreCase(exportCsvValue);
		}
		return exportCsv;
	}
	
	public static boolean parameterType(final MultivaluedMap<String, String> queryParams) {
		boolean parameterType = false;
		if (queryParams.getFirst("parameterType") != null) {
			String parameterTypeValue = queryParams.getFirst("parameterType");
			parameterType = "true".equalsIgnoreCase(parameterTypeValue);
		}
		return parameterType;
	}

	public static boolean template(final MultivaluedMap<String, String> queryParams) {
		boolean template = false;
		if (queryParams.getFirst("template") != null) {
			String prettyPrintValue = queryParams.getFirst("template");
			template = "true".equalsIgnoreCase(prettyPrintValue);
		}
		return template;
	}
	
	public static boolean genericResultSet(final MultivaluedMap<String, String> queryParams) {
		boolean genericResultSet = false;
		if (queryParams.getFirst("genericResultSet") != null) {
			String genericResultSetValue = queryParams.getFirst("genericResultSet");
			genericResultSet = "true".equalsIgnoreCase(genericResultSetValue);
		}
		return genericResultSet;
	}
}