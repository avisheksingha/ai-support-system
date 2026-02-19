package com.aisupport.rule.exception;

public class RuleNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = -4093153611124186715L;

	public RuleNotFoundException(String message) {
        super(message);
    }
}
