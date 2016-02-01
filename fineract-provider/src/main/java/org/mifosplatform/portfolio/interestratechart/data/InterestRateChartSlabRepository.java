/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterestRateChartSlabRepository extends JpaRepository<InterestRateChartSlab, Long>, JpaSpecificationExecutor<InterestRateChartSlab> {

}