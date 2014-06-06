package org.mifosplatform.infrastructure.survey.data;

import java.util.List;

/**
 * Created by Cieyou on 3/11/14.
 */
public class PpiPovertyLineData {

    final String ppi;
    final List<LikeliHoodPovertyLineData> likeliHoodPovertyLineData;


    public PpiPovertyLineData(final List<LikeliHoodPovertyLineData> likeliHoodPovertyLineData,
                       final String ppi){

        this.likeliHoodPovertyLineData = likeliHoodPovertyLineData;
        this.ppi = ppi;

    }


}
