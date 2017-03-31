package com.amtware.toyjms.consumer.flatinfo;

import java.util.stream.Collectors;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.repackaged.com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class FlatInfo {

	private static final int HEADER_LIMIT = 6;
	private static final int WORD_LIMIT = 40;
	
	private final String price;
	private final String header;
	private final String date;
	private final String image;
	private final String link;
	private final String desc;
	private final String shortHeader;
	private final String shortDesc;

	public FlatInfo(String price, String header, String date, String image, String link, String desc) {
		super();
		this.price = price;
		this.header = header;
		this.date = date;
		this.image = image;
		this.link = link;
		this.desc = desc;
		this.shortHeader = ellipse(this.header, HEADER_LIMIT);
		this.shortDesc = ellipse(this.desc, WORD_LIMIT);
	}

	public String getPrice() {
		return price;
	}

	public String getHeader() {
		return header;
	}
	public String getShortHeader() {
		return shortHeader;
	}

	public String getDate() {
		return date;
	}

	public String getImage() {
		return image;
	}

	public String getLink() {
		return link;
	}

	public String getDesc() {
		return desc;
	}
	public String getShortDesc() {
		return shortDesc;
	}

	private String ellipse(String string, int wordLimit) {
		return Joiner.on(" ")
		.join(ImmutableList.copyOf(Splitter.on(" ").split(string)).stream().limit(wordLimit).collect(Collectors.toList()))
		+"...";
	}
}
