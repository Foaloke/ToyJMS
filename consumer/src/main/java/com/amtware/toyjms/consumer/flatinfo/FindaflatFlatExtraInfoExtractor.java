package com.amtware.toyjms.consumer.flatinfo;

import java.io.IOException;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FindaflatFlatExtraInfoExtractor implements ExtraInfoExtractor {
	private static final Logger LOGGER = Logger.getLogger(FindaflatFlatExtraInfoExtractor.class);

	private static final String PREFIX = "https://www.findaflat.com";
	private static final String IMG_PREFIX = "https://photos2.spareroom.co.uk/images/flatshare/listings/large/";

	@Override
	public boolean canExtractFrom(String link) {
		return link.startsWith(PREFIX);
	}

	@Override
	public Optional<ExtraInfo> extractExtraInfo(String link) {
		return downloadResource(link).map(this::readExtraInfoFromDocument);
	}
	
	private ExtraInfo readExtraInfoFromDocument(Document doc){
		String header = doc.select("div.colsinglemiddle h1").first().text();
		String image = IMG_PREFIX+doc.select("dt.mainImage img").first().attr("src").replaceAll("^.*?/([0-9]+/[0-9]+/[0-9]+\\.jpg)$", "$1");
		String desc = doc.select("p.detaildesc").first().text();
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
