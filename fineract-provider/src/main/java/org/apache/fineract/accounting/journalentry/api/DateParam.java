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
package org.apache.fineract.accounting.journalentry.api;

import java.util.Date;
import java.util.Locale;

import javax.ws.rs.WebApplicationException;

import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.joda.time.LocalDate;

/**
 * Class for parsing dates sent as query parameters
 * 
 * TODO: Vishwas Should move this class to a more generic package
 */
public class DateParam {

    private final String dateAsString;

    public DateParam(final String dateStr) throws WebApplicationException {
        this.dateAsString = dateStr;
    }

    public Date getDate(final String parameterName, final String dateFormat, final String localeAsString) {
        final Locale locale = JsonParserHelper.localeFromString(localeAsString);
        final LocalDate localDate = JsonParserHelper.convertFrom(this.dateAsString, parameterName, dateFormat, locale);
        return localDate.toDate();
    }
}
