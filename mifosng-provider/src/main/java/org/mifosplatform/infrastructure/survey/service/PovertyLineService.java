package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.survey.data.LikeliHoodPovertyLineData;
import org.mifosplatform.infrastructure.survey.data.PpiPovertyLineData;

public interface PovertyLineService {

    PpiPovertyLineData retrieveAll(final String ppiName);

    LikeliHoodPovertyLineData retrieveForLikelihood(final String ppiName, final Long likelihood);

}
