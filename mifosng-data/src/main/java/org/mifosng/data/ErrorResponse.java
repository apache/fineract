package org.mifosng.data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Being removed in favour of {@link ApiGlobalErrorResponse} and {@link ApiParameterError}
 */
@Deprecated
@XmlRootElement(name="errorResponse")
public class ErrorResponse {

    private String code;
    private String field;
    private String value;
    private Object[] args;

	public ErrorResponse() {
		//
	}

    public ErrorResponse(final String code, final String field, final String value) {
        this.code = code;
        this.field = field;
        this.value = value;
    }
    
    public ErrorResponse(final String code, final String field, final Object... args) {
        this.code = code;
        this.field = field;
        this.args = args;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getField() {
        return this.field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
}