package com.amtware.toyjms.consumer.flatinfo;

import java.io.IOException;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MoveflatFlatExtraInfoExtractor implements ExtraInfoExtractor {
	private static final Logger LOGGER = Logger.getLogger(MoveflatFlatExtraInfoExtractor.class);

	private static final String PREFIX = "http://www.moveflat.co.uk";

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
		String image = PREFIX + doc.select("tbody tr td img").first().attr("src").replace("X=.*?&Y=.*?&", "");
		String desc = doc.text().split("Description")[1].split("Flatshare interests and occupations")[0];
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
