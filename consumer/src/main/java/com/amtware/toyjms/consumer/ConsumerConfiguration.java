package com.amtware.toyjms.consumer;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import com.amtware.toyjms.configuration.JMSConfiguration;
import com.amtware.toyjms.consumer.flatinfo.ExtraInfoExtractor;
import com.google.common.base.Charsets;

@Configuration
@Import(JMSConfiguration.class)
@ComponentScan(
		basePackages = "com.amtware.toyjms.consumer",
	    includeFilters = @ComponentScan.Filter(
	        type = FilterType.ASSIGNABLE_TYPE,
	        value = ExtraInfoExtractor.class
	    )
	)
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
	
	@Bean
	public freemarker.template.Configuration freeMarkerConfiguration() {
		freemarker.template.Configuration configuration
			= new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
		configuration.setDefaultEncoding(Charsets.UTF_8.displayName());
		configuration.setClassForTemplateLoading(this.getClass(), "/");
		return configuration;		
	}

	@Bean
	public MessageReader messageReader() {
		return new MessageReader();
	}

	@Bean
	public EmailSender emailSender() {
		return new EmailSender();
	}
}
