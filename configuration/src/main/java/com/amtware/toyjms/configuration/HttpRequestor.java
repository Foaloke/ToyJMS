package com.amtware.toyjms.configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.io.CharStreams;

public class HttpRequestor {
	private static final Logger LOGGER = Logger.getLogger(HttpRequestor.class);

	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	public Optional<String> request(String url) {
		return request(url, Optional.empty());
	}

	public Optional<String> request(String url, Optional<String> params) {
		LOGGER.debug("Attempting request to: "+url);
		try {

			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
			GenericUrl gURL = new GenericUrl(url);
			HttpRequest request = requestFactory.buildGetRequest(gURL);
			HttpResponse response = request.execute();

			LOGGER.debug(readParameters(params) + "Received response: " + response.getStatusMessage());

			return Optional.of(CharStreams.toString(new InputStreamReader(response.getContent())));

		} catch (IOException e) {
			LOGGER.debug("Unable to reach " + url, e);
		}
		return Optional.empty();
	}

	private String readParameters(Optional<String> params) {
		return params.map(ps -> "Sent parameters: " + ps + " - ").orElse("");
	}
}
