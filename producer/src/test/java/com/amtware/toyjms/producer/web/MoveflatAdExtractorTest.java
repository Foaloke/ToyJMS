package com.amtware.toyjms.producer.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amtware.toyjms.configuration.HttpRequestor;
import com.amtware.toyjms.producer.web.adinfo.AdInfo;
import com.amtware.toyjms.producer.web.adinfo.extractor.AdInfoExtractor.ResourceInfo;
import com.amtware.toyjms.producer.web.adinfo.extractor.MoveflatPagesAdExtractor;
import com.google.common.io.Resources;

@RunWith(MockitoJUnitRunner.class)
public class MoveflatAdExtractorTest {

	private static final String MOVEFLAT_EXAMPLE = "moveflat_example.txt";
	private static final String MOVEFLAT_SINGLE_EXAMPLE = "moveflat_single_example.txt";

	private static final String POSTCODE = "pcode";
	private static final double LATITUDE = 0.31;
	private static final double LONGITUDE = 0.71;
	
	private static final Optional<String> MOCK_POSTCODE_RESPONSE
		= Optional.of("{\"result\":{\"postcode\": \""+POSTCODE+"\", \"longitude\":"+LONGITUDE+",\"latitude\":"+LATITUDE+"}}");
	
	private static final String EXPECTED_GENERATED_URL
	 = "http://www.moveflat.co.uk/289707arr.aspx"
			+ "?q=s"
			+ "&swlng="+(LONGITUDE-0.1)
			+ "&swlat="+(LATITUDE-0.1)
			+ "&nelng="+(LONGITUDE+0.1)
			+ "&nelat="+(LATITUDE+0.1);
	
	@Mock
	private HttpRequestor httpRequestor;
	
	@InjectMocks
	private MoveflatPagesAdExtractor extractor;

	@Before
	public void setUp() throws IOException, URISyntaxException {
		
		when(httpRequestor.request("http://postcodes.io/postcodes/"+POSTCODE))
			.thenReturn(MOCK_POSTCODE_RESPONSE);

		Path filePath = Paths.get(Resources.getResource(MOVEFLAT_EXAMPLE).toURI());
		String text = new String(Files.readAllBytes(filePath));

		when(httpRequestor.request(EXPECTED_GENERATED_URL))
			.thenReturn(Optional.of(text));

		Path singlePath = Paths.get(Resources.getResource(MOVEFLAT_SINGLE_EXAMPLE).toURI());
		String singleText = new String(Files.readAllBytes(singlePath));
		when(httpRequestor.request("http://www.moveflat.co.uk/c/509408.htm")).thenReturn(Optional.of(singleText));
		
	}

	@Test
	public void whenACommandIsGiven_thenACorrectURLIsFormed() {
		String request = extractor.composeRequest(POSTCODE+",01 Jun 2017,350");

		assertThat(request,equalTo(EXPECTED_GENERATED_URL));
	}

	@Test
	public void whenACommandIsGiven_thenACorrectAdInfoIsFormed() {
		List<AdInfo> info
			= extractor.extractAllResources(EXPECTED_GENERATED_URL, 1)
				.map(ResourceInfo::getResource)
				.collect(Collectors.toList());
		
		assertThat(info.get(0).getPrice(), equalTo("£813"));
		assertThat(info.get(0).getDate(), equalTo(date(29,3,2017)));
		assertThat(info.get(0).getLink(), equalTo("http://www.moveflat.co.uk/c/509408.htm"));
	
	}

	private ZonedDateTime date(int day, int month, int year) {
		return ZonedDateTime.of(year, month, day, 00, 00, 00, 00, ZoneId.systemDefault());
	}
}
