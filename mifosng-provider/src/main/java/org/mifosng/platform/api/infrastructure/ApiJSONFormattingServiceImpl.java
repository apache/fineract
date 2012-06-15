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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiJSONFormattingServiceImpl implements ApiJSONFormattingService {

	private final static Logger logger = LoggerFactory
			.getLogger(ApiJSONFormattingServiceImpl.class);

	@Override
	public String convertRequest(Object dataObject, String filterName,
			String allowedFieldList, String selectedFields,
			MultivaluedMap<String, String> queryParams) {

		String associationFields = "";
		return convertRequestCommon(dataObject, filterName, allowedFieldList,
				selectedFields, associationFields, queryParams);

	}

	@Override
	public String convertRequest(Object dataObject, String filterName,
			String allowedFieldList, String selectedFields,
			String associationFields, MultivaluedMap<String, String> queryParams) {

		return convertRequestCommon(dataObject, filterName, allowedFieldList,
				selectedFields, associationFields, queryParams);

	}

	private String convertRequestCommon(Object dataObject, String filterName,
			String allowedFieldList, String selectedFields,
			String associationFields, MultivaluedMap<String, String> queryParams) {

		// filterType E means Exclude : has the effect of returning all fields
		// in the dataObject but can only be used if selectedFields is "" and
		// there is no fields query parameter
		// filterType I means Include : used when a specific list of fields is
		// to be returned.

		String filterType = "E";
		String fieldList = "";
		String fields = queryParams.getFirst("fields");

		if (isPassed(fields) || (!(selectedFields.equals("")))) {
			filterType = "I";

			if (isPassed(fields)) {
				if (selectedFields.equals(""))
					fieldList = fields;
				else {
					Set<String> paramFieldsSet = createSetFromString(fields);
					Set<String> selectedFieldsSet = createSetFromString(selectedFields);
					fieldList = createIncludedInStringList(paramFieldsSet,
							selectedFieldsSet);
				}
			} else {
				fieldList = selectedFields;
			}

		}

		fieldList = updateListForTemplate(fieldList, allowedFieldList,
				queryParams.getFirst("template"), filterType);

		fieldList = updateListForAssociations(fieldList, associationFields,
				queryParams.getFirst("associations"), filterType);

		// logger.info("fieldList to be processed is: " + fieldList
		// + "   filter type is: " + filterType);
		logger.info("query params: " + queryParams.toString());
		return convertDataObjectJSON(dataObject, filterName, filterType,
				fieldList, isTrue(queryParams.getFirst("pretty")));
	}

	private String updateListForTemplate(String fieldList,
			String allowedFieldList, String param, String filterType) {

		if (isTrue(param)) {
			if (filterType.equals("I")) {
				return fieldList + "," + allowedFieldList;
			}
			return fieldList;
		}

		// No template query param provided
		if (filterType.equals("E")) {
			fieldList = allowedFieldList; // exclude
		}

		return fieldList;

	}

	private String updateListForAssociations(String fieldList,
			String associationFields, String param, String filterType) {

		if (isPassed(param)) {
			//logger.info("is associations");
			if (param.equalsIgnoreCase("ALL")) {
				//logger.info("is ALL");
				if (filterType.equals("I")) {
					//logger.info("is Include");
					return fieldList + "," + associationFields;
				}
				//logger.info("is Exclude");
				return fieldList;
			}

			//logger.info("Not ALL");
			Set<String> fullAssociationsSet = createSetFromString(associationFields);
			Set<String> paramAssociationsSet = createSetFromString(param);

			if (filterType.equals("I")) {

				String selectedAssociationFields = createIncludedInStringList(
						paramAssociationsSet, fullAssociationsSet);
				//logger.info("is Include - selected fields are :"
				//		+ selectedAssociationFields);
				return fieldList + "," + selectedAssociationFields;
			}

			String unSelectedAssociationFields = createNotIncludedInStringList(
					fullAssociationsSet, paramAssociationsSet);
			//logger.info("is Exclude - unselected fields are :"
			//		+ unSelectedAssociationFields);

			if (fieldList.equals("")) {
				return unSelectedAssociationFields;
			}
			return fieldList + "," + unSelectedAssociationFields;
		}

		// No association parameter provided
		// If Exclude - then add the list of associationFields to the original
		// fieldList
		// If filter is Include just return the original fieldList value
		if (filterType.equals("E")) {
			if (fieldList.equals("")) {
				return associationFields;
			}
			return fieldList + "," + associationFields;
		}

		return fieldList;

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

			if (filterType.equals("I")) {
				return new SimpleFilterProvider().addFilter(
						filterName,
						SimpleBeanPropertyFilter
								.filterOutAllExcept(filterFields)).addFilter(
						"permissionFilter",
						SimpleBeanPropertyFilter
								.serializeAllExcept(permissionFields));

			}

			return new SimpleFilterProvider().addFilter(filterName,
					SimpleBeanPropertyFilter.serializeAllExcept(filterFields))
					.addFilter(
							"permissionFilter",
							SimpleBeanPropertyFilter
									.serializeAllExcept(permissionFields));
		}

		if (filterName.equals("userFilter")) {

			Set<String> roleFields = new HashSet<String>();
			Set<String> permissionFields = new HashSet<String>();

			if (filterType.equals("I")) {
				return new SimpleFilterProvider()
						.addFilter(
								filterName,
								SimpleBeanPropertyFilter
										.filterOutAllExcept(filterFields))
						.addFilter(
								"roleFilter",
								SimpleBeanPropertyFilter
										.serializeAllExcept(roleFields))
						.addFilter(
								"permissionFilter",
								SimpleBeanPropertyFilter
										.serializeAllExcept(permissionFields));

			}

			return new SimpleFilterProvider()
					.addFilter(
							filterName,
							SimpleBeanPropertyFilter
									.serializeAllExcept(filterFields))
					.addFilter(
							"roleFilter",
							SimpleBeanPropertyFilter
									.serializeAllExcept(roleFields))
					.addFilter(
							"permissionFilter",
							SimpleBeanPropertyFilter
									.serializeAllExcept(permissionFields));

		}
		return filters;
	}

	private Set<String> createSetFromString(String string) {
		Set<String> set = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(string, ",");
		while (st.hasMoreTokens()) {
			set.add(st.nextToken().trim());
		}
		return set;
	}

	private String createIncludedInStringList(Set<String> candidates,
			Set<String> matchAgainst) {
		String stringList = "";
		Boolean first = true;
		for (String candidate : candidates) {
			if (matchAgainst.contains(candidate)) {
				if (first) {
					stringList = candidate;
					first = false;
				} else {
					stringList += "," + candidate;
				}
			}
		}
		return stringList;
	}

	private String createNotIncludedInStringList(Set<String> candidates,
			Set<String> matchAgainst) {
		String stringList = "";
		Boolean first = true;
		for (String candidate : candidates) {
			if (!(matchAgainst.contains(candidate))) {
				if (first) {
					stringList = candidate;
					first = false;
				} else {
					stringList += "," + candidate;
				}
			}
		}
		return stringList;
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