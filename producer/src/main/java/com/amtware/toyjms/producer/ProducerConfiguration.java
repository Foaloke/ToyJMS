package com.amtware.toyjms.producer;

import java.util.List;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import com.amtware.toyjms.configuration.JMSConfiguration;
import com.amtware.toyjms.producer.web.adinfo.extractor.AdInfoExtractor;
import com.amtware.toyjms.producer.web.webfacade.WebFacade;
import com.amtware.toyjms.producer.web.webfacade.WebFacadeMultiplePages;

@Configuration
@Import(JMSConfiguration.class)
@ComponentScan(
	basePackages = "com.amtware.toyjms.producer",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = AdInfoExtractor.class
    )
)
public class ProducerConfiguration {

	private static final String QUEUE_NAME = "toyjms";

	@Autowired
	Session session;

	@Autowired
	List<AdInfoExtractor<?>> adInfoExtractors;

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
		return this.webFacade().getCommands();
	}

	@Bean
	public WebFacade webFacade() {
		return new WebFacadeMultiplePages(this.adInfoExtractors);
	}

}
