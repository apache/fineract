/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.domain;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HolidayRepository extends JpaRepository<Holiday, Long>, JpaSpecificationExecutor<Holiday> {

    @Query("select holiday from Holiday holiday, IN(holiday.offices) office where (holiday.fromDate >= :date OR :date <= holiday.toDate) and holiday.status = :status and office.id = :officeId")
    List<Holiday> findByOfficeIdAndGreaterThanDate(@Param("officeId") Long officeId, @Param("date") Date date,
            @Param("status") Integer status);

    @Query("from Holiday holiday where holiday.processed = false and holiday.status = :status")
    List<Holiday> findUnprocessed(@Param("status") Integer status);
}
