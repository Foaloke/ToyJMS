package com.amtware.toyjms.consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Consumer implements MessageListener {
	private static final Logger LOGGER = Logger.getLogger(Consumer.class);

	@Autowired
	private MessageConsumer messageConsumer;

	public void run() {
		try {
			this.messageConsumer.setMessageListener(this);
		} catch (JMSException e) {
			throw new IllegalStateException("Could not run the Consumer", e);
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				TextMessage txtMessage = (TextMessage) message;
				System.out.println(txtMessage.getText());
			}
		} catch (JMSException e) {
			LOGGER.warn(
				"Invalid message type received (" + message.getClass().getSimpleName()+")",
				e);
		}
	}

}
