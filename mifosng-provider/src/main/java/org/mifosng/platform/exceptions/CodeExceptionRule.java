package org.mifosng.platform.exceptions;

public class CodeExceptionRule extends AbstractPlatformDomainRuleException
{
	public CodeExceptionRule()
	{
		super("error.msg.code.systemdefined","This code is system defined and cannot be modified or deleted.");
	}
}
