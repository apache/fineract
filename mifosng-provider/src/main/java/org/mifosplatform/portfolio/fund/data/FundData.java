package org.mifosplatform.portfolio.fund.data;

/**
 * Immutable data object to represent fund data.
 */
public class FundData {

    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private String externalId;

    public static FundData instance(final Long id, final String name, final String externalId) {
        return new FundData(id, name, externalId);
    }

    private FundData(final Long id, final String name, final String externalId) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
    }
}