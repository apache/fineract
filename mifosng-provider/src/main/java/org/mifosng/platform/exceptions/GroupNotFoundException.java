package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when group resources are not found.
 */
public class GroupNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GroupNotFoundException(Long id){
        super("error.msg.group.id.invalid", "Group with identifier " + id + " does not exist", id);
    }
    
}
