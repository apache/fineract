package org.mifosng.platform.exceptions;

import org.mifosng.platform.common.ApplicationConstants;

public class ImageNotFoundException extends AbstractPlatformDomainRuleException {

	public ImageNotFoundException(
			ApplicationConstants.IMAGE_MANAGEMENT_ENTITY entityType,
			Long entityId) {
		super("error.msg.entity.image.invalid", "Image for entity "
				+ entityType.toString() + " with Identifier " + entityId
				+ " does not exist", entityType.toString(), entityId);
	}

}
