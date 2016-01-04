/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.repository;

import org.mifosplatform.spm.domain.LookupTable;
import org.mifosplatform.spm.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LookupTableRepository extends JpaRepository<LookupTable, Long> {

    List<LookupTable> findBySurvey(final Survey survey);
    List<LookupTable> findByKey(final String key);
}
