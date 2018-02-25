package com.ruinscraft.chip;

public class InvalidAttributeException extends Exception {

	private static final long serialVersionUID = -1311585638659970156L;

	private final String reason;
	
	public InvalidAttributeException(String reason) {
		this.reason = reason;
	}
	
	public String getReason() {
		return reason;
	}
	
}
