package org.mifosplatform.infrastructure.codes.data;

/**
 * Immutable data object represent code-value data in system.
 */
public class CodeValueData {

    @SuppressWarnings("unused")
    private final Long id;

    @SuppressWarnings("unused")
    private final String name;

    @SuppressWarnings("unused")
    private final Integer position;

    public static CodeValueData instance(final Long id, final String name, final Integer position) {
        return new CodeValueData(id, name, position);
    }

    private CodeValueData(final Long id, final String name, final Integer position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }
}