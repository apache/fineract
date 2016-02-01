/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.client.domain.Client;

public interface NoteWritePlatformService {

    CommandProcessingResult createNote(JsonCommand command);

    CommandProcessingResult updateNote(JsonCommand command);

    CommandProcessingResult deleteNote(JsonCommand command);

    void createAndPersistClientNote(Client client, JsonCommand command);
}
