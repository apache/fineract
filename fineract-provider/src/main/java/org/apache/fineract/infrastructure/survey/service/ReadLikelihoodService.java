/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.survey.data.LikelihoodData;

import java.util.List;

/**
 * Created by Cieyou on 3/12/14.
 */
public interface ReadLikelihoodService {

    public List<LikelihoodData> retrieveAll(final String ppiName);
    public LikelihoodData retrieve(final Long likelihoodId);
}
