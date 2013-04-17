/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

import java.util.Set;

/**
 * A class to encapsulate settings we allow on API that affect how JSON is to be
 * serialized for the response to api call.
 */
public class ApiRequestJsonSerializationSettings {

    private final boolean prettyPrint;
    private final Set<String> parametersForPartialResponse;
    private final boolean template;
    private final boolean makerCheckerable;
    private final boolean includeJson;

    public ApiRequestJsonSerializationSettings(final boolean prettyPrint, final Set<String> parametersForPartialResponse,
            final boolean template, final boolean makerCheckerable, final boolean includeJson) {
        this.prettyPrint = prettyPrint;
        this.parametersForPartialResponse = parametersForPartialResponse;
        this.template = template;
        this.makerCheckerable = makerCheckerable;
        this.includeJson = includeJson;
    }

    public static ApiRequestJsonSerializationSettings from(final boolean prettyPrint, final Set<String> parametersForPartialResponse,
            final boolean template, final boolean makerCheckerable, final boolean includeJson) {

        // FIXME - KW - rather than always creating new objects for this could
        // just send by common ones like, prettyprint=false, empty response
        // parameters
        return new ApiRequestJsonSerializationSettings(prettyPrint, parametersForPartialResponse, template, makerCheckerable, includeJson);
    }

    public boolean isPrettyPrint() {
        return this.prettyPrint;
    }

    public boolean isTemplate() {
        return this.template;
    }

    public boolean isMakerCheckerable() {
        return this.makerCheckerable;
    }

    public boolean isIncludeJson() {
        return this.includeJson;
    }

    public Set<String> getParametersForPartialResponse() {
        return this.parametersForPartialResponse;
    }

    public boolean isPartialResponseRequired() {
        return !this.parametersForPartialResponse.isEmpty();
    }
}