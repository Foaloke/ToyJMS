package com.amtware.toyjms.producer.web.adinfo;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.apache.log4j.Logger;

import com.amtware.toyjms.producer.web.adinfo.extractor.AdInfoExtractor;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.ZoneId;

import static java.time.temporal.ChronoField.NANO_OF_SECOND;

public class AdInfo {
	private static final Logger LOGGER = Logger.getLogger(AdInfo.class);

	public static final DateTimeFormatter DATE_FORMAT
		= new DateTimeFormatterBuilder()
			.appendPattern("dd MMM yyyy")
            .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
            .parseDefaulting(MINUTE_OF_HOUR, MINUTE_OF_HOUR.range().getMinimum())
            .parseDefaulting(SECOND_OF_MINUTE, SECOND_OF_MINUTE.range().getMinimum())
            .parseDefaulting(NANO_OF_SECOND, NANO_OF_SECOND.range().getMinimum())
			.toFormatter()
			.withZone(ZoneId.systemDefault());

	private final String link;
	private ZonedDateTime date;
	private final String price;

	public AdInfo(String link, ZonedDateTime date, String price) {

		if (date.getYear() < 1900) {
			date.withYear(ZonedDateTime.now().getYear());
		}

		this.link = link;
		this.date = date;
		this.price = price;
	}

	public String getLink() {
		return link;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public String getPrice() {
		return price;
	}

	public int getPriceAsInt() {
		return Integer.valueOf(this.price.replace("£", ""));
	}

	@Override
	public String toString() {
		return String.format("%1$s;%2$s;%3$s", price, date.format(DATE_FORMAT), link);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdInfo other = (AdInfo) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		return true;
	}

}
