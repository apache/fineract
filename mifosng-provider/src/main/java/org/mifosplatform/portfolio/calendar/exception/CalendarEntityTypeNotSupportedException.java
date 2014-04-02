package org.mifosplatform.portfolio.calendar.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class CalendarEntityTypeNotSupportedException extends AbstractPlatformResourceNotFoundException {

    public CalendarEntityTypeNotSupportedException(final String resource) {
        super("calendar.entitytype.not.supported", "Calendar does not support resource "+ resource);
    }

}
