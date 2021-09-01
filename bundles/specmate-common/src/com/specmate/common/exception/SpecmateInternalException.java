package com.specmate.common.exception;

import com.specmate.model.administration.ErrorCode;

/**
 * Generic exception to handle internal errors that should be logged, but not
 * exposed to clients.
 *
 */
public class SpecmateInternalException extends SpecmateException {
	private static final long serialVersionUID = -3409999913440740340L;

	public SpecmateInternalException(ErrorCode ecode, String msg) {
		super(ecode, msg);
	}

	public SpecmateInternalException(ErrorCode ecode, Exception cause) {
		super(ecode, cause);
	}

	public SpecmateInternalException(ErrorCode ecode, String msg, Exception cause) {
		super(ecode, msg, cause);
	}
}
