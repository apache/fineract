package org.mifosplatform.commands.handler;

import org.mifosplatform.commands.domain.CommandSource;

public interface CommandSourceHandler {

    /*
     * Used when users with 'create' capability create a command. If
     * 'maker-checker' is not enabled for this specific command then the
     * 'creator' is also marked 'as the checker' and command automatically is
     * processed and changes state of system.
     */
    CommandSource handleCommandWithSupportForRollback(CommandSource commandSource);

    /*
     * Used when users with 'checker' capability approve a command.
     */
    CommandSource handleCommandForCheckerApproval(CommandSource commandSourceResult);
}
