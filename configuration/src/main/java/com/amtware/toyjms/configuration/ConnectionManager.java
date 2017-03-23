package com.amtware.toyjms.configuration;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ConnectionManager {
	private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class);

	private final Connection connection;

	public ConnectionManager(String brokerUrl) {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
			Connection conn = factory.createConnection();
			conn.start();
			this.connection = conn;
		} catch (JMSException e) {
			throw new IllegalStateException("Connection creation failed", e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	@PreDestroy
	public void cleanUp() {
		try {
			LOGGER.debug("Closing connection...");
			if (this.connection != null) {
				this.connection.close();
				LOGGER.debug("Connection closed");
			} else {
				LOGGER.warn("No connection was established");
			}
		} catch (JMSException e) {
			LOGGER.error("Could not close connection", e);
		}
	}
}
