package com.amtware.toyjms.producer.web.httprequestor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Throwables;
import com.google.common.io.CharStreams;

public final class HTTPRequestor {
	private static final Logger logger = Logger.getLogger(HTTPRequestor.class);

	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	protected HTTPRequestor() { }

	public static String request(String url, Optional<String> params) {
		try {

			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
			GenericUrl gURL = new GenericUrl(url);
			HttpContent content = new ByteArrayContent("application/x-www-form-urlencoded",
					params.orElse("").getBytes());
			HttpRequest request = requestFactory.buildPostRequest(gURL, content);
			HttpResponse response = request.execute();

			logger.debug(readParameters(params) + "Received response: " + response.getStatusMessage());

			return CharStreams.toString(new InputStreamReader(response.getContent()));

		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private static String readParameters(Optional<String> params) {
		return params.map(ps -> "Sent parameters: " + ps + " - ").orElse("");
	}
}
