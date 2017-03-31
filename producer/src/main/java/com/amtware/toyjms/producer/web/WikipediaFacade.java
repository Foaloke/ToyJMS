package com.amtware.toyjms.producer.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.amtware.toyjms.configuration.HttpRequestor;
import com.amtware.toyjms.producer.web.webfacade.WebFacade;
import com.google.common.io.Resources;
import com.google.common.net.UrlEscapers;

public final class WikipediaFacade implements WebFacade {
	private static final Logger logger = Logger.getLogger(WikipediaFacade.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String COMMANDS_LIST_PATH = "word_list.txt";

	private HttpRequestor httpRequestor;
	
	public WikipediaFacade(HttpRequestor httpRequestor) {
		this.httpRequestor = httpRequestor;
	}

	@Override
	public List<String> request(String title) {
		return this.httpRequestor.request(composeWikipediaAPIRequest(title))
				.map(this::evaluateWikipediaResponse).orElse(new ArrayList<>());
	}

	private  List<String> evaluateWikipediaResponse(String wikipediaResponse){
		try {

			JsonNode rootNode = OBJECT_MAPPER.readValue(wikipediaResponse, JsonNode.class);
			JsonNode pagesNode = rootNode.get("query").get("pages");
			
			return StreamSupport
						.stream(fieldNamesSpliterator(pagesNode),false)
						.map(extractTextWithIdFrom(pagesNode))
						.map(this::cleanWikipediaRawText)
						.collect(Collectors.toList());

		} catch (IOException e) {
			logger.error("Failed reading a entry from Wikipedia.", e);
		}
		return new ArrayList<>();
	}
	
	@Override
	public Stream<String> getCommands() {
		try {
			return Files.lines(Paths.get(Resources.getResource(COMMANDS_LIST_PATH).toURI()));
		} catch (IOException | URISyntaxException e) {
			throw new IllegalStateException("Request initialization failed", e);
		}
	}

	private Spliterator<String> fieldNamesSpliterator(JsonNode pagesNode) {
		Iterable<String> fieldNamesIterable = pagesNode::getFieldNames;
		return fieldNamesIterable.spliterator();
	}

	private String composeWikipediaAPIRequest(String title) {
		return String.format(
				"https://en.wikipedia.org/w/api.php"
				+ "?action=query"
				+ "&prop=revisions"
				+ "&rvprop=content"
				+ "&titles=%1$s"
				+ "&format=json",
				UrlEscapers.urlPathSegmentEscaper().escape(title));
	}

	private Function<String, String> extractTextWithIdFrom(JsonNode pagesNode) {
		return pageId -> pagesNode.get(pageId).get("revisions").get(0).get("*").asText();
	}

	private String cleanWikipediaRawText(String rawText) {
		return rawText
				.replaceAll("\\[\\[", "")
				.replaceAll("\\]\\]", "")
				.replaceAll("={2,3}.*?={2,3}", "")
				.replaceAll("'{2,3}", "")
				.replaceAll("\\{\\{.*?\\}\\}", "")
				.replaceAll("<ref.*?</ref>", "");
	}

}
