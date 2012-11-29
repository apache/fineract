package org.mifosng.platform.api.data;

public final class TransactionProcessingStrategyData {

    private final Long id;
    private final String code;
    private final String name;

    public TransactionProcessingStrategyData(final Long id, final String code, final String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}