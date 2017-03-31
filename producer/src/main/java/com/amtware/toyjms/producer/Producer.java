package com.amtware.toyjms.producer;

import java.util.List;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amtware.toyjms.producer.web.webfacade.WebFacade;

@Component
public class Producer {
	private static final Logger LOGGER = Logger.getLogger(Producer.class);

	@Autowired
	private MessageProducer messageProducer;

	@Autowired
	private Session session;

	@Autowired
	private Stream<String> requests;

	@Autowired
	private WebFacade webFacade;

	public void run() {
        this.requests.forEachOrdered(this::evaluate);
	}

    private void evaluate(String word){
        LOGGER.info("Evaluating `" + word +"`");
		try {

	        Thread.sleep(500);
	        List<String> all = webFacade.request(word);
	        all.forEach(this::sendMessage);

		} catch (InterruptedException e) {
			LOGGER.warn("Evaluation of `" + word + "` failed", e);
			Thread.currentThread().interrupt();
		}finally{
			LOGGER.info("Evaluation of `" + word + "` terminated");
		}
    }

	private void sendMessage(String message) {
		try {
			LOGGER.warn("Sending `" + message + "`");
			this.messageProducer.send(this.session.createTextMessage(message));
		} catch (JMSException e) {
			LOGGER.warn("Could not send message `" + message + "`", e);
		}
	}

}
