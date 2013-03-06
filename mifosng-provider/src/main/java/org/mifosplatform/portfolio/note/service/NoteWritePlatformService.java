package org.mifosplatform.portfolio.note.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface NoteWritePlatformService {

    CommandProcessingResult createNote(JsonCommand command);

    CommandProcessingResult updateNote(JsonCommand command);

    CommandProcessingResult deleteNote(JsonCommand command);
}
