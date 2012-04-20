package org.mifosng.platform.infrastructure;

import org.apache.commons.lang.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class FirstTimeLoginFormBean {

    private String oldUsername;
    private String username;
    private String password;
	private String successView;

    public void validateUpdateUsernamePasswordDetails(final ValidationContext validationContext) {
        MessageContext messageContext = validationContext.getMessageContext();

        if (StringUtils.isBlank(this.username)) {
            MessageBuilder builder = new MessageBuilder().error().source("username")
            .codes(new String[] { "username.blank" })
            .defaultText("Username cannot be empty.")
            .args(new Object[] {});

            messageContext.addMessage(builder.build());
        }

        if (this.oldUsername.trim().equalsIgnoreCase(this.username.trim())) {
            MessageBuilder builder = new MessageBuilder().error().source("username")
            .codes(new String[] { "username.same.as.old.username" })
            .defaultText("The new username cannot be the same as the old username.")
            .args(new Object[] {});

            messageContext.addMessage(builder.build());
        }

        if (StringUtils.isBlank(this.password)) {
            MessageBuilder builder = new MessageBuilder().error().source("password")
            .codes(new String[] { "password.blank" })
            .defaultText("Password cannot be empty.")
            .args(new Object[] {});

            messageContext.addMessage(builder.build());
        }
    }

    public String getOldUsername() {
        return this.oldUsername;
    }

    public void setOldUsername(final String oldUsername) {
        this.oldUsername = oldUsername;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

	public String getSuccessView() {
		return this.successView;
	}

	public void setSuccessView(final String successView) {
		this.successView = successView;
	}
}