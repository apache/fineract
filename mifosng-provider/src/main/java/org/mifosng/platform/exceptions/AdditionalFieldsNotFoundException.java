package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class AdditionalFieldsNotFoundException extends AbstractPlatformResourceNotFoundException {

	public AdditionalFieldsNotFoundException(final String type, final Long id) {
		super("error.msg.type.value.not.found", "Additional Fields Type: "
				+ type + " Id: " + id + " is not accessible");
	}

	public AdditionalFieldsNotFoundException(final String type, final String set) {
		super("error.msg.set.not.found", "Additional Fields Set Not Found",
				"Type: " + type + "   Set: " + set);
	}
}