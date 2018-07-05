package com.ruinscraft.chip;

import org.bukkit.inventory.meta.BookMeta;

import com.ruinscraft.chip.util.ChipUtil;

public class BookSig {

	private String originalAuthor;
	private String contentSum;
	private long datestamp;
	
	public BookSig(String originalAuthor,String contentSum, long dateStamp) {
		this.originalAuthor = originalAuthor;
		this.contentSum = contentSum;
		this.datestamp = dateStamp;
	}
	
	public String getOriginalAuthor() {
		return originalAuthor;
	}
	
	public String getContentSum() {
		return contentSum;
	}
	
	public long getDatestamp() {
		return datestamp;
	}
	
	public void setContentSum(String contentSum) {
		this.contentSum = contentSum;
	}
	
	public static BookSig create(BookMeta bookMeta) {
		return new BookSig(bookMeta.getAuthor(), ChipUtil.getMd5HashOfBookContent(bookMeta), System.currentTimeMillis());
	}
	
	public static BookSig create(BookMeta bookMeta, String diffAuthorName) {
		return new BookSig(diffAuthorName, ChipUtil.getMd5HashOfBookContent(bookMeta), System.currentTimeMillis());
	}
	
}
