/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.api;

import java.util.Date;
import java.util.Locale;

import javax.ws.rs.WebApplicationException;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;

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

    public Date getDate(String parameterName,String dateFormat, String localeAsString) {
        Locale locale = JsonParserHelper.localeFromString(localeAsString);
        LocalDate localDate = JsonParserHelper.convertFrom(dateAsString, parameterName, dateFormat, locale);
        return localDate.toDate();
    }
}
