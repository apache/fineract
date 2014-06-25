package org.mifosplatform.batch.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.springframework.transaction.TransactionException;

/**
 * Provides an Error Handler method that returns an object of type {@link ErrorInfo}
 * to the CommandStrategy which raised the exception. This class uses various subclasses
 * of RuntimeException to check the kind of exception raised and provide appropriate
 * status and error codes for each one of the raised exception.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.command.CommandStrategy
 * @see org.mifosplatform.batch.command.internal.CreateClientCommandStrategy 
 */
public class ErrorHandler extends RuntimeException{
		
	/**
	 * Sole Constructor 
	 */
	ErrorHandler() {
        super();
    }
	
	/**
	 * Returns an object of ErrorInfo type containing the information regarding 
	 * the raised error.
	 * 
	 * @param exception
	 * @return ErrorInfo
	 */
	public static ErrorInfo handler(RuntimeException exception) {
		
		if(exception instanceof AbstractPlatformResourceNotFoundException) {
			return new ErrorInfo(404, 1001, "Requested Resource was not Found: Check for any unavailable Parameters");
		} else if(exception instanceof UnsupportedParameterException) {
			return new ErrorInfo(400, 2001, "UnsupportedParameterException: Check for any unsupported Parameters");
		} else if(exception instanceof PlatformApiDataValidationException) {
			return new ErrorInfo(400, 2002, "ApiDataValidationException: Re-Check the input data");
		} else if(exception instanceof PlatformDataIntegrityException) {
			return new ErrorInfo(403, 3001, "DataIntegrityException: Check for any duplicate data");
		} else if(exception instanceof TransactionException) {
			return new ErrorInfo(400, 4001, "TransactionException: One or more of your requests were unsuccessful," +
					" reverting back all the transactions in this Batch Request");
		}
	    
		return new ErrorInfo(500, 9999, "Internal Server Error");
	      	
	}
}
