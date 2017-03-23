package com.amtware.toyjms.consumer;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.amtware.toyjms.configuration.JMSConfiguration;

@Configuration
@Import(JMSConfiguration.class)
@ComponentScan("com.amtware.toyjms.consumer")
public class ConsumerConfiguration {

	private static final String QUEUE_NAME = "toyjms";

	@Autowired
	Session session;

	@Bean
	public MessageConsumer messageConsumer() {
		try {
			return this.session.createConsumer(this.session.createQueue(QUEUE_NAME));
		} catch (JMSException e) {
			throw new IllegalStateException("Message Consumer creation failed", e);
		}
	}

}
