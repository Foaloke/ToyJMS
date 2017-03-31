package com.amtware.toyjms.consumer;

import java.util.Observable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Consumer extends Observable implements MessageListener {
	private static final Logger LOGGER = Logger.getLogger(Consumer.class);

	@Autowired
	private MessageConsumer messageConsumer;

	@Autowired
	private MessageReader messageReader;

	@Autowired
	private EmailSender emailSender;

	public void run() {
		try {
			this.addObserver(this.emailSender);
			this.messageConsumer.setMessageListener(this);
		} catch (JMSException e) {
			throw new IllegalStateException("Could not run the Consumer", e);
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				String text = ((TextMessage) message).getText();

				LOGGER.debug("Received: "+text);

				this.setChanged();
				messageReader.readFromMessage(text).ifPresent(this::notifyObservers);

			}
		} catch (JMSException e) {
			LOGGER.warn(
				"Invalid message type received (" + message.getClass().getSimpleName()+")",
				e);
		}
	}
}
