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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.amtware.toyjms.configuration.HttpRequestor;
import com.amtware.toyjms.producer.web.adinfo.AdInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class MoveflatPagesAdExtractor implements AdInfoExtractor<AdInfo> {
	private static final Logger LOGGER = Logger.getLogger(MoveflatPagesAdExtractor.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Pattern AD_DATE_PATTERN = Pattern.compile("Avail [A-z]+? ([0-9]{1,2} [A-z]+?)\\s");

    private static final DateTimeFormatter MOVEFLAT_DATE_FORMAT
		= new DateTimeFormatterBuilder()
			.parseLenient()
			.appendPattern("dd MMMM")
            .parseDefaulting(YEAR, ZonedDateTime.now().getYear())
            .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
            .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
            .parseDefaulting(MINUTE_OF_HOUR, MINUTE_OF_HOUR.range().getMinimum())
            .parseDefaulting(SECOND_OF_MINUTE, SECOND_OF_MINUTE.range().getMinimum())
            .parseDefaulting(NANO_OF_SECOND, NANO_OF_SECOND.range().getMinimum())
			.toFormatter()
			.withZone(ZoneId.systemDefault());

	private HttpRequestor httpRequestor;
	
	public MoveflatPagesAdExtractor(HttpRequestor httpRequestor) {
		this.httpRequestor = httpRequestor;
	}

	@Override
	public String composeRequest(String command) {
		String[] commands = command.split(",");
		String postcode = commands[0];
		LatitudeLongitude postcodeCoordinates = getLatitudeLongitudeOf(postcode);
		return "http://www.moveflat.co.uk/289707arr.aspx"
				+ "?q=s"
				+ "&swlng=" + postcodeCoordinates.getSWLongitude()
				+ "&swlat=" + postcodeCoordinates.getSWLatitude()
				+ "&nelng=" + postcodeCoordinates.getNELongitude()
				+ "&nelat=" + postcodeCoordinates.getNELatitude();
	}

	@Override
	public Stream<ResourceInfo<AdInfo>> extractAllResources(String url, int limit) {
		return Arrays.stream(evaluateUrl(url)).limit(limit).map(this::readAdLine);
	}

	@Override
	public List<AdInfo> extractAdsInfoFromResource(ResourceInfo<AdInfo> resource, ZonedDateTime date, int price) {
		List<AdInfo> result = new ArrayList<>();
		searchDateAndPrice(resource.getResource(), date, price).ifPresent(result::add);
		return result;
	}

	private String[] evaluateUrl(String url) {
		return this.httpRequestor.request(url).map(text -> this.groupBy(text.split(","), 4)).orElse(new String[0]);
	}

	private ResourceInfo<AdInfo> readAdLine(String adLine){
		String[] adData = adLine.split(",");
		String descriptor = adData[2];
		String adCode = adData[3];

		String price = "£"+(descriptor.split("£"))[1];
		String link = "http://www.moveflat.co.uk/c/"+adCode+".htm";
		Optional<String> text = this.httpRequestor.request(link);
		
		ZonedDateTime date = ZonedDateTime.now();
		if(text.isPresent()){
		    Matcher dateMatcher = AD_DATE_PATTERN.matcher(text.get());
		    if(dateMatcher.find()){
		    	date = ZonedDateTime.parse(dateMatcher.group(1), MOVEFLAT_DATE_FORMAT);
		    }
		}

		return new ResourceInfo<>(new AdInfo(link, date, price), Optional.empty());
	}

	private Optional<AdInfo> searchDateAndPrice(AdInfo resource, ZonedDateTime date, int price) {
		return Optional.of(resource)
					.filter(this.adIsAfter(date))
					.filter(this.adIsNotTooLate(date))
					.filter(this.weeklyPriceIsLessThan(price));
	}

	private LatitudeLongitude getLatitudeLongitudeOf(String postcode) {
		try {
			String url = "http://postcodes.io/postcodes/" + postcode;
			JsonNode root
				= OBJECT_MAPPER.readValue(this.httpRequestor.request(url).orElseThrow(coordinatesException(postcode)), JsonNode.class);
			return new LatitudeLongitude(
					root.get("result").get("latitude").asDouble(),
					root.get("result").get("longitude").asDouble());
		} catch (IOException e) {
			throw coordinatesException(postcode).get();
		}
	}

	private Predicate<AdInfo> adIsAfter(ZonedDateTime date){
		 return r -> r.getDate().isAfter(date);
	}

	private Predicate<AdInfo> adIsNotTooLate(ZonedDateTime date){
		 return r -> r.getDate().isBefore(date.plusMonths(1L));
	}

	private Predicate<AdInfo> weeklyPriceIsLessThan(int price){
		return r -> r.getPriceAsInt() < price*4;
	}

	private String[] groupBy(String[] results, int size) {
		List<String> groups = new ArrayList<>();
		String[] slice;
		for(int i=0; i+4<results.length; i+=size){
			slice = new String[4];
			System.arraycopy(results, i, slice, 0, size);
			groups.add(Joiner.on(",").join(slice));
		}
		return Iterables.toArray(groups, String.class);
	}

	private static class LatitudeLongitude {
		private static final double WINDOW_SIZE = 0.2;
		private final double latitude;
		private final double longitude;
		public LatitudeLongitude(double latitude, double longitude) {
			super();
			this.latitude = latitude;
			this.longitude = longitude;
		}
		public double getSWLatitude() {
			return this.latitude-WINDOW_SIZE/2;
		}
		public double getSWLongitude() {
			return this.longitude-WINDOW_SIZE/2;
		}
		public double getNELatitude() {
			return this.latitude+WINDOW_SIZE/2;
		}
		public double getNELongitude() {
			return this.longitude+WINDOW_SIZE/2;
		}
	}

	private Supplier<IllegalStateException> coordinatesException(String postcode) {
		return coordinatesException(postcode, null);
	}
	private Supplier<IllegalStateException> coordinatesException(String postcode, Exception e){
		return () -> new IllegalStateException("Could not find coordinates of "+postcode, e);
	}
}
