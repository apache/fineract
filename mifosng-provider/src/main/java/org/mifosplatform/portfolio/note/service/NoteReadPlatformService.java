/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.service;

import java.util.Collection;

import org.mifosplatform.portfolio.note.data.NoteData;

public interface NoteReadPlatformService {

    NoteData retrieveNote(final Long noteId, Long resourceId, Integer noteTypeId);

    Collection<NoteData> retrieveNotesByResource(final Long resourceId, final Integer noteTypeId);
}