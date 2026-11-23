/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.jobs.filter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.springframework.http.HttpMethod;

/**
 * Recognises the loan recalculation command ({@code POST /loans/{loanId}?command=recalculate}, or the external id
 * variant), which the progressive model check and the loan close of business API filters let through untouched: the
 * command replaces the state those filters would rebuild or catch up, and applies the lock rule itself.
 *
 * <p>
 * The match is deliberately narrow. Only a POST to the loan itself counts, and only the first {@code command} query
 * parameter is read, since that is the one JAX-RS hands to the resource. Anything else, such as a PUT to the loan or a
 * POST to one of its sub-resources carrying the same query parameter, still goes through the filters.
 */
final class LoanRecalculationRequestMatcher {

    private static final Pattern LOAN_PATH_PATTERN = Pattern.compile("/v[1-9][0-9]*/loans/(?:external-id/)?[^/?]+/?");
    private static final String COMMAND_PARAMETER = "command";

    private LoanRecalculationRequestMatcher() {}

    /**
     * @param method
     *            the HTTP method of the request
     * @param path
     *            the path of the request, without the query string
     * @param queryString
     *            the raw query string of the request, or null
     */
    static boolean isRecalculation(final String method, final String path, final String queryString) {
        return HttpMethod.POST.matches(method) && path != null && LOAN_PATH_PATTERN.matcher(path).matches()
                && CommandParameterUtil.is(firstCommandParameter(queryString), LoanApiConstants.RECALCULATE_COMMAND);
    }

    /**
     * @param method
     *            the HTTP method of a batch item
     * @param relativeUrl
     *            the relative URL of a batch item, with its query string
     */
    static boolean isRecalculation(final String method, final String relativeUrl) {
        if (relativeUrl == null) {
            return false;
        }
        final int queryStart = relativeUrl.indexOf('?');
        if (queryStart < 0) {
            return false;
        }
        return isRecalculation(method, relativeUrl.substring(0, queryStart), relativeUrl.substring(queryStart + 1));
    }

    private static String firstCommandParameter(final String queryString) {
        if (StringUtils.isBlank(queryString)) {
            return null;
        }
        for (final String pair : StringUtils.split(queryString, '&')) {
            final int separator = pair.indexOf('=');
            final String name = decode(separator < 0 ? pair : pair.substring(0, separator));
            if (COMMAND_PARAMETER.equals(name)) {
                return separator < 0 ? "" : decode(pair.substring(separator + 1));
            }
        }
        return null;
    }

    private static String decode(final String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Not valid percent-encoding: JAX-RS would not decode it to the command name either
            return value;
        }
    }
}
