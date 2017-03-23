package com.amtware.toyjms.consumer;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

	private App() { }

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context
			= new AnnotationConfigApplicationContext(ConsumerConfiguration.class);
		
		Consumer consumer = context.getBean(Consumer.class);
		consumer.run();

		context.close();
	}

}
