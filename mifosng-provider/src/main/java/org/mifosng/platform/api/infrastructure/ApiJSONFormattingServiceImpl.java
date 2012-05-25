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
	public String convertRequest(Object dataObject, String allowedFieldList,
			String selectedFields, MultivaluedMap<String, String> queryParams) {

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
					st = new StringTokenizer(fields, ",");
					while (st.hasMoreTokens()) {
						selectedFieldsSet.add(st.nextToken().trim());
					}

					Boolean first = true;
					for (String paramField : paramFieldsSet) {
						if (selectedFieldsSet.contains(paramField)) {
							if (first) {
								fieldList = paramField;
								first = false;
							} else
								fieldList += "," + paramField;
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

		return convertDataObjectJSON(dataObject, filterType, fieldList,
				isTrue(queryParams.getFirst("pretty")));
	}

	private String convertDataObjectJSON(Object dataObject, String filterType,
			String fields, boolean prettyOutput) {

		try {
			String json = "";
			String myFilter = "myFilter";
			FilterProvider filters = null;
			Set<String> filterFields = new HashSet<String>();

			StringTokenizer st = new StringTokenizer(fields, ",");
			while (st.hasMoreTokens()) {
				filterFields.add(st.nextToken().trim());
			}

			if (filterType.equals("I")) {
				filters = new SimpleFilterProvider().addFilter(myFilter,
						SimpleBeanPropertyFilter
								.filterOutAllExcept(filterFields));

			} else {
				filters = new SimpleFilterProvider().addFilter(myFilter,
						SimpleBeanPropertyFilter
								.serializeAllExcept(filterFields));
			}

			ObjectWriter jsonWriter = null;
			if (prettyOutput) {
				jsonWriter = new ObjectMapper()
						.writerWithDefaultPrettyPrinter();
			} else {
				jsonWriter = new ObjectMapper().writer();
			}

			json = jsonWriter.withFilters(filters).writeValueAsString(
					dataObject);
			return json;
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

	private Boolean isTrue(String param) {
		if (param != null && param.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}

	private Boolean isPassed(String param) {
		if (param == null || param.equals(""))
			return false;
		else
			return true;
	}

}