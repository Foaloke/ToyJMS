package com.amtware.toyjms.producer;

import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amtware.toyjms.producer.web.WikipediaFacade;

@Component
public class Producer {
	private static final Logger LOGGER = Logger.getLogger(Producer.class);

	@Autowired
	private MessageProducer messageProducer;

	@Autowired
	private Session session;

	@Autowired
	private Stream<String> requests;

	public void run() {
        this.requests.forEachOrdered(this::evaluate);
	}

    private void evaluate(String word){
        LOGGER.info("Evaluating `" + word +"`");
		try {

	        Thread.sleep(500);
	        this.messageProducer
        		.send(this.session.createTextMessage(WikipediaFacade.request(word)));

		} catch (JMSException | InterruptedException e) {
			LOGGER.warn("Evaluation of `" + word + "` failed", e);
		}finally{
			LOGGER.info("Evaluation of `" + word + "` terminated");
		}
    }

}
