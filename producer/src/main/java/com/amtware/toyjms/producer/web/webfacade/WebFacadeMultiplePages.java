package com.amtware.toyjms.producer.web.webfacade;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.amtware.toyjms.producer.web.adinfo.AdInfo;
import com.amtware.toyjms.producer.web.adinfo.extractor.AdInfoExtractor;
import com.google.common.io.Resources;

public class WebFacadeMultiplePages implements WebFacade {
	private static final Logger LOGGER = Logger.getLogger(WebFacadeMultiplePages.class);
	private static final int MAX_SUBMISSIONS = 50;

    private static final String COMMANDS_LIST_PATH = "flats_list.txt";

	private Collection<AdInfoExtractor<?>> adInfoExtractors;

	public WebFacadeMultiplePages(Collection<AdInfoExtractor<?>> adInfoExtractor) {
		this.adInfoExtractors = adInfoExtractor;
	}

	@Override
	public List<String> request(String command) {
		ExecutorService executor
		= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<String> results = this.adInfoExtractors.stream()
									.flatMap(singleExtractorRequest(executor, command))
									.collect(Collectors.toList());
		executor.shutdown();
		return results;
	}

	public Function<AdInfoExtractor<?>, Stream<String>> singleExtractorRequest(ExecutorService executor, String command) {
		return extractor -> 
				extractor
					.paralleliseExtraction(command, executor, MAX_SUBMISSIONS)
					.flatMap(this::getFutureResultAsStream)
					.distinct()
					.map(AdInfo::toString);
	}

	@Override
	public Stream<String> getCommands() {
		try {
			return Files.lines(Paths.get(Resources.getResource(COMMANDS_LIST_PATH).toURI()));
		} catch (IOException | URISyntaxException e) {
			throw new IllegalStateException("Request initialization failed", e);
		}
	}

	private Stream<AdInfo> getFutureResultAsStream(Future<List<AdInfo>> future) {
		try {
			return future.get().stream();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Failed to get results from thread "+future.toString(), e);
		}
		return Stream.empty();
	}

}
