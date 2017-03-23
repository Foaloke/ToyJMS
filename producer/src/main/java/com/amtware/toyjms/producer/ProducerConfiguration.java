package com.amtware.toyjms.producer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.amtware.toyjms.configuration.JMSConfiguration;
import com.google.common.io.Resources;

@Configuration
@Import(JMSConfiguration.class)
@ComponentScan("com.amtware.toyjms.producer")
public class ProducerConfiguration {

    private static final String WORDS_LIST_PATH = "word_list.txt";

	private static final String QUEUE_NAME = "toyjms";

	@Autowired
	Session session;

	@Bean
	public MessageProducer messageProducer() {
		try {
			return this.session.createProducer(this.session.createQueue(QUEUE_NAME));
		} catch (JMSException e) {
			throw new IllegalStateException("Message Producer creation failed", e);
		}
	}

	@Bean
	public Stream<String> initRequests() {
		try {
			return Files.lines(Paths.get(Resources.getResource(WORDS_LIST_PATH).toURI()));
		} catch (IOException | URISyntaxException e) {
			throw new IllegalStateException("Request initialization failed", e);
		}
	}

}
