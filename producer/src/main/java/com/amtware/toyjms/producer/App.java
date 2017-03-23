package com.amtware.toyjms.producer;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

	public static void main(String[] args) throws Exception {

		AnnotationConfigApplicationContext context
			= new AnnotationConfigApplicationContext(ProducerConfiguration.class);

		Producer producer = context.getBean(Producer.class);
		producer.run();
		context.close();
	}
}
