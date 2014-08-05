/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface LikelihoodRepository extends JpaRepository<Likelihood, Long>, JpaSpecificationExecutor<Likelihood> {

   @Query("FROM Likelihood WHERE ppi_name =:ppiName AND id !=:id")
   List<Likelihood> findByPpiNameAndLikeliHoodId(@Param("ppiName") String ppiName,@Param("id") Long likeliHoodId);

}
