package org.mifosplatform.accounting.glaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidParentGLAccountHeadException extends AbstractPlatformDomainRuleException {

	public InvalidParentGLAccountHeadException(Long glAccountId, Long parentId) {
		super("error.msg.glaccount.id.and.parentid.must.not.same", "parentId:"+parentId+", id"+glAccountId+" should not be same", glAccountId, parentId);
	}

}
