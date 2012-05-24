package org.mifosng.platform.api.infrastructure;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

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

	private final static Logger logger = LoggerFactory.getLogger(ApiJSONFormattingServiceImpl.class);
	
	@Override
	public String convertDataObjectJSON(Object dataObject, String fields, Set<String> excludeFields, boolean prettyOutput) {

		try {
			String json = "";
			String myFilter = "myFilter";
			Set<String> includeFields = new HashSet<String>();
			FilterProvider filters = null;

			if (fields == null || fields.equals("")) {
				filters = new SimpleFilterProvider().addFilter(myFilter,
						SimpleBeanPropertyFilter.serializeAllExcept(excludeFields));

			} else {

				StringTokenizer st = new StringTokenizer(fields, ",");
				while (st.hasMoreTokens()) {
					includeFields.add(st.nextToken().trim());
				}

				filters = new SimpleFilterProvider().addFilter(myFilter,
						SimpleBeanPropertyFilter.filterOutAllExcept(includeFields));
			}
			
			ObjectWriter jsonWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
			json = jsonWriter.withFilters(filters).writeValueAsString(dataObject);
			return json;
		} catch (JsonGenerationException e) {
			throw new PlatformInternalServerException("error.msg.platform.json.generation", "An error occured whilst generating response from the platform server.", e.getMessage(), e.getStackTrace());
		} catch (JsonMappingException e) {
			throw new PlatformInternalServerException("error.msg.platform.json.generation", "An error occured whilst generating response from the platform server.", e.getMessage(), e.getStackTrace());
		} catch (IOException e) {
			throw new PlatformInternalServerException("error.msg.platform.json.generation", "An error occured whilst generating response from the platform server.", e.getMessage(), e.getStackTrace());
		}
	}
}