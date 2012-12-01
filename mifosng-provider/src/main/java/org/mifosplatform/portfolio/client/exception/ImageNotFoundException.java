package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ImageNotFoundException extends AbstractPlatformDomainRuleException {

    public ImageNotFoundException(final String resource, final Long resourceId) {
        super("error.msg.entity.image.invalid", "Image for resource " + resource + " with Identifier " + resourceId + " does not exist",
                resource, resourceId);
    }
}