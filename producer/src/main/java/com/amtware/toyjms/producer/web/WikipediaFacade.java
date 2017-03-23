package com.amtware.toyjms.producer.web;

import java.io.IOException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.amtware.toyjms.producer.web.httprequestor.HTTPRequestor;
import com.google.common.net.UrlEscapers;

public final class WikipediaFacade {
	private static final Logger logger = Logger.getLogger(WikipediaFacade.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static String request(String title) {
		try {

			String plainText = HTTPRequestor.request(composeWikipediaAPIRequest(title), Optional.empty());
			JsonNode rootNode = OBJECT_MAPPER.readValue(plainText, JsonNode.class);
			JsonNode pagesNode = rootNode.get("query").get("pages");
			
			return StreamSupport
						.stream(fieldNamesSpliterator(pagesNode),false)
						.map(extractTextWithIdFrom(pagesNode))
						.reduce((a,b) -> a+"\n"+b)
						.map(WikipediaFacade::cleanWikipediaRawText)
						.orElse("");

		} catch (IOException e) {
			logger.error("Failed reading the entry " + title + " from Wikipedia.", e);
		}
		return null;
	}

	private static Spliterator<String> fieldNamesSpliterator(JsonNode pagesNode) {
		Iterable<String> fieldNamesIterable = pagesNode::getFieldNames;
		return fieldNamesIterable.spliterator();
	}

	private static String composeWikipediaAPIRequest(String title) {
		return String.format(
				"https://en.wikipedia.org/w/api.php"
				+ "?action=query"
				+ "&prop=revisions"
				+ "&rvprop=content"
				+ "&titles=%1$s"
				+ "&format=json",
				UrlEscapers.urlPathSegmentEscaper().escape(title));
	}

	private static Function<String, String> extractTextWithIdFrom(JsonNode pagesNode) {
		return pageId -> pagesNode.get(pageId).get("revisions").get(0).get("*").asText();
	}

	private static String cleanWikipediaRawText(String rawText) {
		return rawText
				.replaceAll("\\[\\[", "")
				.replaceAll("\\]\\]", "")
				.replaceAll("={2,3}.*?={2,3}", "")
				.replaceAll("'{2,3}", "")
				.replaceAll("\\{\\{.*?\\}\\}", "")
				.replaceAll("<ref.*?</ref>", "");
	}
}
