package org.mifosng.platform.api.infrastructure;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.mifosng.platform.exceptions.PlatformInternalServerException;
import org.springframework.stereotype.Service;

@Service
public class ApiJSONFormattingServiceImpl implements ApiJSONFormattingService {

	@Override
	public String convertRequest(Object dataObject, String filterName,
			String allowedFieldList, String selectedFields,
			MultivaluedMap<String, String> queryParams) {

		String filterType = "E";
		String fieldList = "";
		String fields = queryParams.getFirst("fields");

		if (isPassed(fields) || (!(selectedFields.equals("")))) {
			filterType = "I";

			if (isPassed(fields)) {
				if (selectedFields.equals(""))
					fieldList = fields;
				else {
					Set<String> paramFieldsSet = new HashSet<String>();
					StringTokenizer st = new StringTokenizer(fields, ",");
					while (st.hasMoreTokens()) {
						paramFieldsSet.add(st.nextToken().trim());
					}
					Set<String> selectedFieldsSet = new HashSet<String>();
					st = new StringTokenizer(selectedFields, ",");
					while (st.hasMoreTokens()) {
						selectedFieldsSet.add(st.nextToken().trim());
					}

					Boolean first = true;
					for (String paramField : paramFieldsSet) {
						if (selectedFieldsSet.contains(paramField)) {
							if (first) {
								fieldList = paramField;
								first = false;
							} else {
								fieldList += "," + paramField;
							}
						}
					}
				}
			} else {
				fieldList = selectedFields;
			}

		}

		if (isTrue(queryParams.getFirst("template"))) {
			if (filterType.equals("I"))
				fieldList += "," + allowedFieldList;
		} else {
			if (filterType.equals("E"))
				fieldList = allowedFieldList;
		}

		return convertDataObjectJSON(dataObject, filterName, filterType,
				fieldList, isTrue(queryParams.getFirst("pretty")));
	}

	private String convertDataObjectJSON(Object dataObject, String filterName,
			String filterType, String fields, boolean prettyOutput) {

		try {

			FilterProvider filters = buildFilter(filterName, filterType, fields);
			ObjectWriter jsonWriter = null;
			if (prettyOutput) {
				jsonWriter = new ObjectMapper()
						.writerWithDefaultPrettyPrinter();
			} else {
				jsonWriter = new ObjectMapper().writer();
			}

			return jsonWriter.withFilters(filters).writeValueAsString(
					dataObject);
		} catch (JsonGenerationException e) {
			throw new PlatformInternalServerException(
					"error.msg.platform.json.generation",
					"An error occured whilst generating response from the platform server.",
					e.getMessage(), e.getStackTrace());
		} catch (JsonMappingException e) {
			throw new PlatformInternalServerException(
					"error.msg.platform.json.generation",
					"An error occured whilst generating response from the platform server.",
					e.getMessage(), e.getStackTrace());
		} catch (IOException e) {
			throw new PlatformInternalServerException(
					"error.msg.platform.json.generation",
					"An error occured whilst generating response from the platform server.",
					e.getMessage(), e.getStackTrace());
		}
	}

	private FilterProvider buildFilter(String filterName, String filterType,
			String fields) {
		FilterProvider filters = null;

		Set<String> filterFields = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(fields, ",");
		while (st.hasMoreTokens()) {
			filterFields.add(st.nextToken().trim());
		}

		if (filterName.equals("loanFilter")) {

			Set<String> loanRepaymentFields = new HashSet<String>();

			if (filterType.equals("I")) {
				return new SimpleFilterProvider().addFilter(
						filterName,
						SimpleBeanPropertyFilter
								.filterOutAllExcept(filterFields)).addFilter(
						"loanRepaymentFilter",
						SimpleBeanPropertyFilter
								.serializeAllExcept(loanRepaymentFields));

			}

			return new SimpleFilterProvider().addFilter(filterName,
					SimpleBeanPropertyFilter.serializeAllExcept(filterFields))
					.addFilter(
							"loanRepaymentFilter",
							SimpleBeanPropertyFilter
									.serializeAllExcept(loanRepaymentFields));
		}

		if ((filterName.equals("loanRepaymentFilter"))
				|| (filterName.equals("myFilter"))
				|| (filterName.equals("permissionFilter"))) {

			if (filterType.equals("I")) {
				return new SimpleFilterProvider().addFilter(filterName,
						SimpleBeanPropertyFilter
								.filterOutAllExcept(filterFields));

			}

			return new SimpleFilterProvider().addFilter(filterName,
					SimpleBeanPropertyFilter.serializeAllExcept(filterFields));
		}

		if (filterName.equals("roleFilter")) {

			Set<String> permissionFields = new HashSet<String>();
			// Ask keith which ones to show
			permissionFields.add("id");
			permissionFields.add("name");
			permissionFields.add("description");
			permissionFields.add("code");
			permissionFields.add("groupType");

			if (filterType.equals("I")) {
				return new SimpleFilterProvider().addFilter(
						filterName,
						SimpleBeanPropertyFilter
								.filterOutAllExcept(filterFields)).addFilter(
						"permissionFilter",
						SimpleBeanPropertyFilter
								.filterOutAllExcept(permissionFields));

			}

			return new SimpleFilterProvider().addFilter(filterName,
					SimpleBeanPropertyFilter.serializeAllExcept(filterFields))
					.addFilter(
							"permissionFilter",
							SimpleBeanPropertyFilter
									.filterOutAllExcept(permissionFields));
		}

		if (filterName.equals("userFilter")) {

			Set<String> roleFields = new HashSet<String>();
			roleFields.add("id");
			roleFields.add("name");
			roleFields.add("description");
			roleFields.add("availablePermissions");
			roleFields.add("selectedPermissions");
			// Ask keith which ones to show
			Set<String> permissionFields = new HashSet<String>();
			// Ask keith which ones to show
			permissionFields.add("id");
			permissionFields.add("name");
			permissionFields.add("description");
			permissionFields.add("code");
			permissionFields.add("groupType");

			if (filterType.equals("I")) {
				return new SimpleFilterProvider()
						.addFilter(
								filterName,
								SimpleBeanPropertyFilter
										.filterOutAllExcept(filterFields))
						.addFilter(
								"roleFilter",
								SimpleBeanPropertyFilter
										.filterOutAllExcept(roleFields))
						.addFilter(
								"permissionFilter",
								SimpleBeanPropertyFilter
										.filterOutAllExcept(permissionFields));

			}

			return new SimpleFilterProvider()
					.addFilter(
							filterName,
							SimpleBeanPropertyFilter
									.serializeAllExcept(filterFields))
					.addFilter(
							"roleFilter",
							SimpleBeanPropertyFilter
									.filterOutAllExcept(roleFields))
					.addFilter(
							"permissionFilter",
							SimpleBeanPropertyFilter
									.filterOutAllExcept(permissionFields));

		}
		return filters;
	}

	private Boolean isTrue(String param) {
		if (param != null && param.equalsIgnoreCase("true"))
			return true;

		return false;
	}

	private Boolean isPassed(String param) {
		if (param == null || param.equals(""))
			return false;

		return true;
	}

}