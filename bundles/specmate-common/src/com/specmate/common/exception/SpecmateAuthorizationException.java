package com.specmate.common.exception;

import com.specmate.model.administration.ErrorCode;

public class SpecmateAuthorizationException extends SpecmateException {
	private static final long serialVersionUID = -716629287876042669L;

	public SpecmateAuthorizationException(String msg) {
		super(ErrorCode.NO_AUTHORIZATION, msg);
	}

	public SpecmateAuthorizationException(String msg, Exception cause) {
		super(ErrorCode.NO_AUTHORIZATION, msg, cause);
	}

}
