package org.mifosplatform.infrastructure.core.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the successful result of an REST API call.
 * 
 * FIXME - KW - Rename to CommandProcessingResult - make immutable
 */
public class EntityIdentifier {

    private Long commandId;
    private Long resourceId;
    private Long subResourceId;

    private Map<String, Object> changes;

    public static EntityIdentifier commandOnlyResult(final Long commandId) {
        return new EntityIdentifier(null, null, commandId, null);
    }

    public static EntityIdentifier resourceResult(final Long resourceId, final Long commandId) {
        return new EntityIdentifier(resourceId, null, commandId, null);
    }
    
    public static EntityIdentifier resourceResult(final Long resourceId, final Long commandId, final Map<String, Object> changes) {
        return new EntityIdentifier(resourceId, null, commandId, changes);
    }
    
    public static EntityIdentifier subResourceResult(final Long resourceId, final Long subResourceId, final Long commandId) {
        return new EntityIdentifier(resourceId, subResourceId, commandId, null);
    }
    
    public static EntityIdentifier subResourceResult(final Long resourceId, final Long subResourceId, final Long commandId, final Map<String, Object> changes) {
        return new EntityIdentifier(resourceId, subResourceId, commandId, changes);
    }

    public static EntityIdentifier withChanges(final Long resourceId, final Map<String, Object> changes) {
        return new EntityIdentifier(resourceId, null, null, changes);
    }

    public static EntityIdentifier empty() {
        return new EntityIdentifier(Long.valueOf(-1), Long.valueOf(-1), Long.valueOf(-1), null);
    }

    protected EntityIdentifier() {
        //
    }

    public EntityIdentifier(final Long entityId) {
        this.resourceId = entityId;
        this.changes = new HashMap<String, Object>();
    }

    private EntityIdentifier(final Long resourceId, final Long subResourceId, final Long commandId, final Map<String, Object> changesOnly) {
        this.resourceId = resourceId;
        this.subResourceId = subResourceId;
        this.commandId = commandId;
        this.changes = changesOnly;
    }

    public Long commandId() {
        return this.commandId;
    }

    public Long resourceId() {
        return this.resourceId;
    }

    public Long subResourceId() {
        return this.subResourceId;
    }

    public Map<String, Object> getChanges() {
        Map<String, Object> checkIfEmpty = null;
        if (this.changes != null && !this.changes.isEmpty()) {
            checkIfEmpty = this.changes;
        }
        return checkIfEmpty;
    }

    public boolean hasChanges() {
        final boolean noChanges = this.changes == null || this.changes.isEmpty();
        return !noChanges;
    }
}