package com.amtware.toyjms.configuration;

import javax.jms.JMSException;
import javax.jms.Session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JMSConfiguration {

	public static final String BROKER_URL = "tcp://localhost:61616";

	@Bean
	public ConnectionManager connectionManager() {
		return new ConnectionManager(BROKER_URL);
	}

	@Bean
	public Session session() {
		try {
			return this.connectionManager()
						.getConnection()
						.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			throw new IllegalStateException("Session creation failed", e);
		}
	}

	@Bean
	public HttpRequestor httpRequestor() {
		return new HttpRequestor();
	}

}
