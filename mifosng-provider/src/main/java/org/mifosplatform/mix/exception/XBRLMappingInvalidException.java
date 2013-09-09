package org.mifosplatform.mix.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class XBRLMappingInvalidException extends AbstractPlatformDomainRuleException {

    public XBRLMappingInvalidException(String msg) {
        super("error.msg.xbrl.report.mapping.invalid.id", "Mapping does not exist", msg);
    }

}
