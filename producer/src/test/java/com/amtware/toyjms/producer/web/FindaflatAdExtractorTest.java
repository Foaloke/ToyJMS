package com.amtware.toyjms.producer.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

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
import com.amtware.toyjms.producer.web.adinfo.extractor.FindaflatAdExtractor;
import com.amtware.toyjms.producer.web.adinfo.extractor.AdInfoExtractor.ResourceInfo;
import com.google.common.io.Resources;

public class FindaflatAdExtractorTest {

	private static final String FINDAFLAT_EXAMPLE_PATH = "findaflat_example.htm";
	
	private FindaflatAdExtractor extractor = new FindaflatAdExtractor();
	private Document doc;
	
	@Before
	public void setUp() throws IOException, URISyntaxException {

		Path filePath = Paths.get(Resources.getResource(FINDAFLAT_EXAMPLE_PATH).toURI());
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
			= "https://www.findaflat.com/rent/search.pl"
					+ "?searchtype=advanced"
					+ "&flatshare_type=flatstorent"
					+ "&location_type=area"
					+ "&search=ec3v4ab"
					+ "&miles_from_max=4"
					+ "&min_rent="
					+ "&max_rent=350"
					+ "&per=pw"
					+ "&min_beds="
					+ "&available_search=Y"
					+ "&day_avail=10"
					+ "&mon_avail=6"
					+ "&year_avail=2017"
					+ "&keyword="
					+ "&editing="
					+ "&mode=list"
					+ "&nmsq_mode="
					+ "&action=search"
					+ "&templateoveride="
					+ "&submit=";
		assertThat(request, equalTo(expectedURL));
	}

	@Test
	public void whenACommandIsGiven_thenACorrectAdInfoIsFormed() {
		ResourceInfo<Document> res = new ResourceInfo<>(doc, Optional.empty());		
		List<AdInfo> info = extractor.extractAdsInfoFromResource(res, date(1,4,2017), 350);
		
		assertThat(info.size(), equalTo(1));
		assertThat(info.get(0).getPrice(), equalTo("£345pw"));
		assertThat(info.get(0).getDate(), equalTo(date(3,4,2017)));
		assertThat(info.get(0).getLink(), startsWith("https://www.findaflat.com/rent/fad_click.pl?fad_id=7515643"));
	}

	private ZonedDateTime date(int day, int month, int year) {
		return ZonedDateTime.of(year, month, day, 00, 00, 00, 00, ZoneId.systemDefault());
	}
}
