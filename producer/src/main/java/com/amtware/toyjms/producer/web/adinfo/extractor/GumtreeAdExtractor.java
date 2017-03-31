package com.amtware.toyjms.producer.web.adinfo.extractor;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.amtware.toyjms.producer.web.adinfo.AdInfo;
import com.google.common.collect.Streams;

public class GumtreeAdExtractor implements AdInfoExtractor<Document> {
	private static final Logger LOGGER = Logger.getLogger(GumtreeAdExtractor.class);
	
	private static final String GUMTREE_PREFIX = "https://www.gumtree.com";

    private static final DateTimeFormatter GUMTREE_DATE_FORMAT
		= new DateTimeFormatterBuilder()
			.parseLenient()
			.appendPattern("dd MMM yyyy")
            .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
            .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
            .parseDefaulting(MINUTE_OF_HOUR, MINUTE_OF_HOUR.range().getMinimum())
            .parseDefaulting(SECOND_OF_MINUTE, SECOND_OF_MINUTE.range().getMinimum())
            .parseDefaulting(NANO_OF_SECOND, NANO_OF_SECOND.range().getMinimum())
			.toFormatter()
			.withZone(ZoneId.systemDefault());

    @Override
	public String composeRequest(String command) {

		String[] commands = command.split(",");
		String postcode = commands[0];
		int price = Integer.parseInt(commands[2]);
		
		return "https://www.gumtree.com/search"
				+ "?search_category=flats-and-houses-for-rent"
				+ "&search_location="+postcode
				+ "&seller_type=private"
				+ "&q="
				+ "&distance=10"
				+ "&min_price="
				+ "&max_price="+price
				+ "&min_property_number_beds=1"
				+ "&max_property_number_beds=1";
	}

	@Override
	public Stream<ResourceInfo<Document>> extractAllResources(String url, int limit){
		ResourceInfo<Document> resource = downloadResource(url);
		return Streams.concat(
				Stream.of(resource),
				resource.getNextResourceUrl()
						.map(nextResourceUrl -> this.extractAllResources(nextResourceUrl, limit-1))
						.orElse(Stream.empty()));
				
	}

	@Override
	public List<AdInfo> extractAdsInfoFromResource(ResourceInfo<Document> resInfo, ZonedDateTime date, int price){
		Elements ads = resInfo.getResource().select("#srp-results [data-q^=\"ad\"]");
		return ads.stream()
					.map(this::toAdInfo)
					.filter(this.adIsAfter(date))
					.filter(this.adIsNotTooLate(date))
					.collect(Collectors.toList());		
	}

	private AdInfo toAdInfo(Element adElement) {
		return new AdInfo(
				getLink(adElement),
				getDate(adElement),
				getPrice(adElement)
			);
	}

	private String getLink(Element adElement) {
		String link = adElement.select(".listing-link").attr("href");
		return fixLink(link);
	}

	private ZonedDateTime getDate(Element adElement) {
		String dateText = (adElement
				.select(".listing-attributes")
				.first().child(0).select("span").get(1).text()
				.split(":"))[1].trim();
		return ZonedDateTime.parse(dateText, GUMTREE_DATE_FORMAT);
	}

	private String getPrice(Element adElement) {
		return adElement.select(".listing-price strong").text();
	}

	private ResourceInfo<Document> downloadResource(String url){
		LOGGER.debug("Connecting to: " + url);
		try {

			Document doc = Jsoup.connect(url).get();
			doc.select("script").remove();
			doc.select("style").remove();
			return new ResourceInfo<>(doc, this.extractNextPageLink(doc));

		} catch (IOException e) {
			LOGGER.debug("Could not connect to "+url, e);
		}
		return new ResourceInfo<>(Jsoup.parse(""), Optional.empty());
	}

	private Optional<String> extractNextPageLink(Document doc) {
		return Optional.of(doc.select("li.pagination-next a").attr("href"))
				.filter(s -> !s.isEmpty())
				.map(this::fixLink);
	}

	private Predicate<AdInfo> adIsAfter(ZonedDateTime date) {
		return ad -> ad.getDate().isAfter(date);
	}

	private Predicate<AdInfo> adIsNotTooLate(ZonedDateTime date) {
		return ad -> ad.getDate().isBefore(date.plusMonths(1L));
	}

	private String fixLink(String link) {
		if(!link.startsWith(GUMTREE_PREFIX)){
			return GUMTREE_PREFIX+link;
		}
		return link;
	}

}
