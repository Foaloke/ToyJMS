package com.amtware.toyjms.consumer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amtware.toyjms.consumer.flatinfo.ExtraInfoExtractor;
import com.amtware.toyjms.consumer.flatinfo.ExtraInfoExtractor.ExtraInfo;
import com.amtware.toyjms.consumer.flatinfo.FlatInfo;

@Component
public class MessageReader {
	private static final Logger LOGGER = Logger.getLogger(MessageReader.class);

	@Autowired
	private List<ExtraInfoExtractor> extraInfoExtractors;

	public Optional<FlatInfo> readFromMessage(String message){
		String[] messageParts = message.split(";");
		String price = messageParts[0].trim();
		String date = messageParts[1].trim();
		String link = messageParts[2].trim();
		
		Optional<ExtraInfo> data
			= extraInfoExtractors.stream()
				.filter(canExtractFrom(link))
				.flatMap(extractExtraInfoFrom(link))
				.findFirst();
		
		if(data.isPresent()) {
			return Optional.of(
					new FlatInfo(
							price,
							data.get().getHeader(),
							date,
							data.get().getImage(),
							link,
							data.get().getDesc()));
		}
		
		LOGGER.debug("Could not read data from "+message);
		return Optional.empty();
	}

	private Predicate<ExtraInfoExtractor> canExtractFrom(String link) {
		LOGGER.debug("Looking for a reader of "+link);
		return extractor -> extractor.canExtractFrom(link);
	}

	private Function<ExtraInfoExtractor, Stream<ExtraInfo>> extractExtraInfoFrom(String link) {
		return extractor -> extractor.extractExtraInfo(link).map(Stream::of).orElse(Stream.empty());
	}
}
