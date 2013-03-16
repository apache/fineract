/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object represent client identity data.
 */
public class ClientIdentifierData {

	private final Long id;
	private final Long clientId;
	private final CodeValueData documentType;
	private final String documentKey;
	private final String description;
	@SuppressWarnings("unused")
	private final Collection<CodeValueData> allowedDocumentTypes;

	public static ClientIdentifierData singleItem(final Long id,
			final Long clientId, final CodeValueData documentType,
			final String documentKey, final String description) {
		return new ClientIdentifierData(id, clientId, documentType,
				documentKey, description, null);
	}

	public static ClientIdentifierData template(
			final Collection<CodeValueData> codeValues) {
		return new ClientIdentifierData(null, null, null, null, null,
				codeValues);
	}

	public static ClientIdentifierData template(
			final ClientIdentifierData data,
			final Collection<CodeValueData> codeValues) {
		return new ClientIdentifierData(data.id, data.clientId,
				data.documentType, data.documentKey, data.description,
				codeValues);
	}

	public ClientIdentifierData(final Long id, final Long clientId,
			final CodeValueData documentType, final String documentKey,
			final String description,
			final Collection<CodeValueData> allowedDocumentTypes) {
		this.id = id;
		this.clientId = clientId;
		this.documentType = documentType;
		this.documentKey = documentKey;
		this.description = description;
		this.allowedDocumentTypes = allowedDocumentTypes;
	}
}