package com.amtware.toyjms.producer.web.adinfo.extractor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.amtware.toyjms.producer.web.adinfo.AdInfo;

public interface AdInfoExtractor<R> {

	String composeRequest(String command);

	Stream<ResourceInfo<R>> extractAllResources(String url, int limit);
	
	List<AdInfo> extractAdsInfoFromResource(ResourceInfo<R> resource, ZonedDateTime date, int price);

	default Stream<Future<List<AdInfo>>> paralleliseExtraction(
			String command,
			ExecutorService executor,
			int maxSubmissions) {
		String url = composeRequest(command);
		final String[] commands = command.split(",");
		final ZonedDateTime date = ZonedDateTime.parse(commands[1], AdInfo.DATE_FORMAT);
		final int price = Integer.valueOf(commands[2]);
		return extractAllResources(url, maxSubmissions)
				.map(res -> executor.submit(() -> extractAdsInfoFromResource(res, date, price)));
	}
	
	public static class ResourceInfo<R> {
		private final R resource;
		private final Optional<String> nextResourceUrl;
		public ResourceInfo(R resource, Optional<String> nextResourceUrl) {
			super();
			this.resource = resource;
			this.nextResourceUrl = nextResourceUrl;
		}
		public R getResource() {
			return resource;
		}
		public Optional<String> getNextResourceUrl() {
			return nextResourceUrl;
		}

	}
}
