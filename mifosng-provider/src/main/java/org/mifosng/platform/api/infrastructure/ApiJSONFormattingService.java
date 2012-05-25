package org.mifosng.platform.api.infrastructure;

import java.util.Set;

public interface ApiJSONFormattingService {

	String convertDataObjectJSON(Object dataObject, String filterType,
			String fields, boolean prettyOutput);

	Boolean isTrue(String param);

	Boolean isPassed(String param);

}
