package org.mifosplatform.organisation.office.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * Exception thrown when an attempt is made update the parent of a root office.
 */
public class RootOfficeParentCannotBeUpdated extends
		AbstractPlatformDomainRuleException {

	public RootOfficeParentCannotBeUpdated() {
		super("error.msg.office.cannot.update.parent.office.of.root.office", "The root office must not be set with a parent office.");
	}
}
