package com.amtware.toyjms.consumer;

import java.util.Observable;
import java.util.Observer;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

	private App() { }

	
	
	public static void main(String[] args) {

		AnnotationConfigApplicationContext context
			= new AnnotationConfigApplicationContext(ConsumerConfiguration.class);
		
		Consumer consumer = context.getBean(Consumer.class);		
		consumer.addObserver(new ContextCloser(context));
		consumer.run();

	}

	private static class ContextCloser implements Observer {
		AnnotationConfigApplicationContext context;		
		public ContextCloser(AnnotationConfigApplicationContext context){
			this.context = context;
		}		
		@Override
		public void update(Observable o, Object arg) {
			if(o instanceof Consumer && arg instanceof Boolean && ((Boolean)arg).booleanValue()){				
				context.close();
			}		
		}
	}
}
