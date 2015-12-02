/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.repository;

import org.mifosplatform.spm.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("select s from Survey s where :pointInTime between s.validFrom and s.validTo")
    List<Survey> fetchActiveSurveys(@Param("pointInTime") final Date pointInTime);

    Survey findByKey(final String key);
}
