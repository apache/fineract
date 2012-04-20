package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UpdateUsernamePasswordCommand {

	private boolean usernameToBeChanged = true;
	private String oldUsername;
	private String username;
	private String password;

	protected UpdateUsernamePasswordCommand() {
		//
	}

	public UpdateUsernamePasswordCommand(final String oldUsername,
			final String username, final String password) {
		this.oldUsername = oldUsername;
		this.username = username;
		this.password = password;
	}

	public boolean isUsernameToBeChanged() {
		return this.usernameToBeChanged;
	}

	public void setUsernameToBeChanged(final boolean usernameToBeChanged) {
		this.usernameToBeChanged = usernameToBeChanged;
	}

	public String getOldUsername() {
		return this.oldUsername;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setOldUsername(final String oldUsername) {
		this.oldUsername = oldUsername;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public void setPassword(final String password) {
		this.password = password;
	}
}