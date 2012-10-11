package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when a client Identifer of the particular
 * type is already present
 */
public class DuplicateClientIdentifierException extends
		AbstractPlatformDomainRuleException {

	public DuplicateClientIdentifierException(String identifierType) {
		super("error.msg.clientIdentifier.type.duplicate",
				"Client Identifier of type " + identifierType
						+ " is already present for this client", identifierType);
	}

	public DuplicateClientIdentifierException(String clientName,
			String officeName, String identifierType, String identifierKey) {
		super("error.msg.clientIdentifier.identityKey.duplicate", "Client "
				+ clientName + "under " + officeName + " Branch already has a "
				+ identifierType + " with unique key " + identifierKey,
				clientName, officeName, identifierType, identifierKey);
	}
}