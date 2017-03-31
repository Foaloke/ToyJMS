package com.amtware.toyjms.producer.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import com.amtware.toyjms.producer.web.adinfo.AdInfo;
import com.amtware.toyjms.producer.web.adinfo.extractor.AdInfoExtractor.ResourceInfo;
import com.amtware.toyjms.producer.web.adinfo.extractor.GumtreeAdExtractor;
import com.google.common.io.Resources;

abstract public class GumtreeAdExtractorTest {

	private static final String GUMTREE_EXAMPLE_PATH = "gumtree_example.htm";

	private Document doc;

	private GumtreeAdExtractor extractor = new GumtreeAdExtractor();

	@Before
	public void setUp() throws IOException, URISyntaxException {

		Path filePath = Paths.get(Resources.getResource(GUMTREE_EXAMPLE_PATH).toURI());
		String text = new String(Files.readAllBytes(filePath));
		
		Document doc = Jsoup.parse(text);
		doc.select("script").remove();
		doc.select("style").remove();
		
		this.doc = doc;
		
	}

	@Test
	public void whenACommandIsGiven_thenACorrectURLIsFormed() {
		String request = extractor.composeRequest("ec3v4ab,01 Jun 2017,350");
		String expectedURL
			= "https://www.gumtree.com/search"
				+ "?search_category=flats-and-houses-for-rent"
				+ "&search_location=ec3v4ab"
				+ "&seller_type=private"
				+ "&q="
				+ "&distance=3"
				+ "&min_price="
				+ "&max_price=350"
				+ "&min_property_number_beds=1"
				+ "&max_property_number_beds=1";		
		assertThat(request,equalTo(expectedURL));
	}

	@Test
	public void whenACommandIsGiven_thenACorrectAdInfoIsFormed() {
		ResourceInfo<Document> res = new ResourceInfo<>(doc, Optional.empty());		
		List<AdInfo> info = extractor.extractAdsInfoFromResource(res, date(1,5,2017), 350);
		
		assertThat(info.size(), equalTo(3));
		assertThat(info.get(0).getPrice(), equalTo("£1,400pm"));
		assertThat(info.get(0).getDate(), equalTo(date(2,5,2017)));
		assertThat(info.get(0).getLink(), equalTo("https://www.gumtree.com/p/1-bedroom-rent/1-bedroom-flat-bethnal-green-e2/1226865809"));
		assertThat(info.get(1).getPrice(), equalTo("£1,200pm"));
		assertThat(info.get(1).getDate(), equalTo(date(14,5,2017)));
		assertThat(info.get(1).getLink(), equalTo("https://www.gumtree.com/p/1-bedroom-rent/pretty-1-bedroom-garden-flat-to-let-in-peckham-5-mins-walk-from-queens-road-peckham-train-station/1226779092"));
		assertThat(info.get(2).getPrice(), equalTo("£335pw"));
		assertThat(info.get(2).getDate(), equalTo(date(8,5,2017)));
		assertThat(info.get(2).getLink(), equalTo("https://www.gumtree.com/p/1-bedroom-rent/one-bedroom-flat-with-stunning-views-with-gym-pool-and-free-parking-/1219996583"));
	}

	private ZonedDateTime date(int day, int month, int year) {
		return ZonedDateTime.of(year, month, day, 00, 00, 00, 00, ZoneId.systemDefault());
	}
}
