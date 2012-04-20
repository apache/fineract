package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ChangePasswordCommand {

	private String password;
	private String passwordrepeat;

	public ChangePasswordCommand() {
		//
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordrepeat() {
		return passwordrepeat;
	}

	public void setPasswordrepeat(String passwordrepeat) {
		this.passwordrepeat = passwordrepeat;
	}
}