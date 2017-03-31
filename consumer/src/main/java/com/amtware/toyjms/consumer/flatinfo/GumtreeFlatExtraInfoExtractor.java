package com.amtware.toyjms.consumer.flatinfo;

import java.io.IOException;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GumtreeFlatExtraInfoExtractor implements ExtraInfoExtractor {
	private static final Logger LOGGER = Logger.getLogger(GumtreeFlatExtraInfoExtractor.class);

	private static final String PREFIX = "https://www.gumtree.com";
	private static final String DEFAULT_IMAGE = "https://upload.wikimedia.org/wikipedia/commons/a/ac/No_image_available.svg";

	@Override
	public boolean canExtractFrom(String link) {
		return link.startsWith(PREFIX);
	}

	@Override
	public Optional<ExtraInfo> extractExtraInfo(String link) {
		return downloadResource(link).map(this::readExtraInfoFromDocument);
	}
	
	private ExtraInfo readExtraInfoFromDocument(Document doc){
		String header = doc.select("main header h1").first().text();
		String image = Optional.ofNullable(doc.select("#vip-tabs-images ul li img").first()).map(e -> e.attr("src")).orElse(DEFAULT_IMAGE);
		String desc = doc.select("p.ad-description").get(0).text();
		return new ExtraInfo(header, image, desc);
	}

	private Optional<Document> downloadResource(String url){
		LOGGER.debug("Connecting to: " + url);
		try {

			Document doc = Jsoup.connect(url).get();
			doc.select("script").remove();
			doc.select("style").remove();
			return Optional.of(doc);

		} catch (IOException e) {
			LOGGER.debug("Could not connect to "+url, e);
		}
		return Optional.empty();
	}
}
