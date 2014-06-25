package org.mifosplatform.infrastructure.survey.data;

import org.joda.time.LocalDate;

/**
 * Created by Cieyou on 3/18/14.
 */
public class ClientScoresOverview {

    @SuppressWarnings("unused")
    final private String surveyName;
    @SuppressWarnings("unused")
    final private long id;
    @SuppressWarnings("unused")
    final private String likelihoodCode;
    @SuppressWarnings("unused")
    final private String likelihoodName;
    @SuppressWarnings("unused")
    final private long score;
    @SuppressWarnings("unused")
    final private Double povertyLine;
    @SuppressWarnings("unused")
    final private LocalDate date;

    public ClientScoresOverview(final String likelihoodCode, final String likelihoodName, final long score, final Double povertyLine,
            final LocalDate date, final long resourceId, final String surveyName) {

        this.likelihoodCode = likelihoodCode;
        this.likelihoodName = likelihoodName;
        this.score = score;
        this.povertyLine = povertyLine;
        this.date = date;
        this.id = resourceId;
        this.surveyName = surveyName;

    }
}
