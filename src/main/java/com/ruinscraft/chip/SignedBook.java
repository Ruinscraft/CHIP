package com.ruinscraft.chip;

public class SignedBook {

	private String originalAuthor;
	private long datestamp;
	
	public SignedBook(String originalAuthor, long dateStamp) {
		this.originalAuthor = originalAuthor;
		this.datestamp = dateStamp;
	}
	
	public String getOriginalAuthor() {
		return originalAuthor;
	}
	
	public long getDatestamp() {
		return datestamp;
	}
	
}
