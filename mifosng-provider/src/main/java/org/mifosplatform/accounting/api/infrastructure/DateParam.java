package org.mifosplatform.accounting.api.infrastructure;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

/**
 * Class for parsing dates sent as query parameters
 * 
 * TODO: Vishwas Should move this class to a more generic package
 * 
 * @author
 * 
 */
public class DateParam {

    private final Date date;

    public DateParam(String dateStr) throws WebApplicationException {
        if (StringUtils.isEmpty(dateStr)) {
            this.date = null;
            return;
        }
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity("Couldn't parse date string: Expected format yyyy-mm-dd " + e.getMessage()).build());
        }
    }

    public Date getDate() {
        return date;
    }
}
