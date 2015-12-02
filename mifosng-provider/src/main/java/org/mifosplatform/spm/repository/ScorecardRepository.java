/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.repository;

import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.spm.domain.Scorecard;
import org.mifosplatform.spm.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScorecardRepository extends JpaRepository<Scorecard, Long> {

    List<Scorecard> findBySurvey(final Survey survey);
    List<Scorecard> findBySurveyAndClient(final Survey survey, final Client client);
}
