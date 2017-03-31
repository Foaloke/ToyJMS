package com.amtware.toyjms.producer.web.adinfo.extractor;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.amtware.toyjms.producer.web.adinfo.AdInfo;
import com.google.common.collect.Streams;

public class FindaflatAdExtractor implements AdInfoExtractor<Document> {
	private static final Logger LOGGER = Logger.getLogger(FindaflatAdExtractor.class);

    private static final DateTimeFormatter FINDAFLAT_DATE_FORMAT
		= new DateTimeFormatterBuilder()
			.appendPattern("d MMM")
            .parseDefaulting(YEAR, ZonedDateTime.now().getYear())
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
		ZonedDateTime date = ZonedDateTime.parse(commands[1], AdInfo.DATE_FORMAT);
		int price = Integer.parseInt(commands[2]);

		return "https://www.findaflat.com/rent/search.pl"
				+ "?searchtype=advanced"
				+ "&flatshare_type=flatstorent"
				+ "&location_type=area"
				+ "&search="+postcode
				+ "&miles_from_max=4"
				+ "&min_rent="
				+ "&max_rent="+price
				+ "&per=pw"
				+ "&min_beds="
				+ "&available_search=Y"
				+ "&day_avail=10"
				+ "&mon_avail="+date.getMonthValue()
				+ "&year_avail="+date.getYear()
				+ "&keyword="
				+ "&editing="
				+ "&mode=list"
				+ "&nmsq_mode="
				+ "&action=search"
				+ "&templateoveride="
				+ "&submit=";
	}

	@Override
	public Stream<ResourceInfo<Document>> extractAllResources(String url, int limit){
		ResourceInfo<Document> resource = downloadResource(url);
		return Streams.concat(
				Stream.of(resource),
				resource.getNextResourceUrl()
						.filter(nextResourceUrl -> limit > 0)
						.map(nextResourceUrl -> this.extractAllResources(nextResourceUrl, limit-1))
						.orElse(Stream.empty()));
				
	}

	@Override
	public List<AdInfo> extractAdsInfoFromResource(ResourceInfo<Document> resInfo, ZonedDateTime date, int price){
		Elements rows = resInfo.getResource().select(".listingstable tbody tr");
		List<AdInfo> adInfo = new ArrayList<>();

		AdInfoBuilder builder = AdInfoBuilder.builder(date);
		rows.forEach(row -> {
			Optional<AdInfo> ad  = builder.readInfoOnRowAndReturnAdWhenFinished(row);
			if(ad.isPresent()){
				adInfo.add(ad.get());
			}
		});
		
		return adInfo;
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
		return Optional.of(doc.select("ul.navnext li a").attr("href"))
				.filter(s -> !s.isEmpty())
				.map(link -> "https://www.findaflat.com/rent/"+link);
	}

	private static class AdInfoBuilder {
		private final ZonedDateTime requiredDate;

		private String link;
		private ZonedDateTime date;
		private String price;
		
		private AdInfoBuilder(ZonedDateTime requiredDate){
			this.requiredDate = requiredDate;
		}
		public static AdInfoBuilder builder(ZonedDateTime requiredDate) {
			return new AdInfoBuilder(requiredDate);
		}
		
		public Optional<AdInfo> readInfoOnRowAndReturnAdWhenFinished(Element element){

			if(element.select("td[colspan=4]").size()==1){
				if(this.link == null || this.date == null || this.price == null) {
					throw new IllegalStateException("Missing information while creating ad info");
				}
				Optional<AdInfo> foundAd = Optional.of(new AdInfo(this.link, this.date, this.price));
				this.reset();
				return foundAd
							.filter(adIsAfter(this.requiredDate))
							.filter(adIsNotTooLate(this.requiredDate));
			}

			Elements linkElement = element.select(".table_listing_more");
			Elements dateElement = element.select(".table_listing_availability");
			Elements roomsAndPrice = element.select(".table_listing_rooms");

			if(!linkElement.isEmpty()){
				String linkText = linkElement.select("a").first().attr("href");
				if(!linkText.startsWith("https://www.findaflat.com")){
					linkText = "https://www.findaflat.com"+linkText;
				}
				this.link = linkText;
			}
			if(!dateElement.isEmpty()){
				String dateTimeText = dateElement.text().trim();
				if("Available Now".equals(dateTimeText)) {
					this.date = ZonedDateTime.now();
				}else{
					this.date = ZonedDateTime.parse(dateTimeText, FINDAFLAT_DATE_FORMAT);
				}
			}
			if(!roomsAndPrice.isEmpty()){
				this.price = roomsAndPrice.text().replaceAll(".*?(£[0-9]+).*?(pcm|pw).*?","$1$2");
			}

			return Optional.empty();
		}
		private void reset() {
			this.link = null;
			this.date = null;
			this.price = null;
		}
		private Predicate<AdInfo> adIsAfter(ZonedDateTime date) {
			return ad -> ad.getDate().isAfter(date);
		}

		private Predicate<AdInfo> adIsNotTooLate(ZonedDateTime date) {
			return ad -> ad.getDate().isBefore(date.plusMonths(1L));
		}

	}

}
