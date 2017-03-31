package com.amtware.toyjms.consumer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedTransferQueue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amtware.toyjms.consumer.flatinfo.FlatInfo;
import com.google.common.collect.ImmutableList;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class EmailSender implements Observer {
	private static final Logger LOGGER = Logger.getLogger(EmailSender.class);
	private static final long WAITING_TIME = 120000L;

	public enum State { IDLE, COUNTING_DOWN };
	
	@Autowired
	private Configuration freeMarkerConfiguration;

	private State state = State.IDLE;
	private final Timer timer = new Timer();
	private final Queue<FlatInfo> queue = new LinkedTransferQueue<>();

	@Override
	public void update(Observable o, Object arg) {
		LOGGER.debug("Got an update from "+o.getClass().getSimpleName()+" with "+arg.getClass().getSimpleName());
		if(o instanceof Consumer) {
			LOGGER.debug("Adding message to queue");
			queue.add((FlatInfo) arg);
			if(this.state == State.IDLE){
				this.state = State.COUNTING_DOWN;
				waitAndSend();
			}
		}
	}

	private void waitAndSend() {
		this.timer.schedule(this.createTask(), WAITING_TIME);
	}

	private TimerTask createTask() {
		return new TimerTask() {
			@Override
			public void run() {
				LOGGER.debug("Countdown finished. Sending messages...");
				List<FlatInfo> flatInfo = new ArrayList<>();
				FlatInfo info = queue.poll();
				while(info!=null){
					flatInfo.add(info);
					info = queue.poll();
				}
				if(!flatInfo.isEmpty()){
					sendEmail(flatInfo);
					LOGGER.debug("Messages sent");
				}else{
					LOGGER.debug("No message to send");
				}
				state = State.IDLE;
			}
		};
	}

	private void sendEmail(List<FlatInfo> flatInfo){
		fillInTemplateWith(flatInfo).ifPresent(this::sendEmail);
	}

	private void sendEmail(String html) {
		LOGGER.debug("Sending message");

		List<String> to = ImmutableList.of("matteo.tonnicchi@gmail.com","miguelsanchizperales@gmail.com");
		String from = "supermonito@gmail.com";

		Properties props = System.getProperties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");


		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("supermimonito", "HombreYa!");
			}
		  });

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			to.forEach(addAddressToMessage(message));			
			message.setSubject("Hola! Soy monito!");
			message.setContent(html, "text/html");
			Transport.send(message);
			LOGGER.debug("Message sent successfully");

		} catch (MessagingException e) {
			LOGGER.debug("Could not send message", e);
		}
	}

	private Optional<String> fillInTemplateWith(List<FlatInfo> flatInfo){
		try {

			Map<String, Object> input = new HashMap<>();
			input.put("bannerText", "Hombre Ya!");
			input.put("flatInfo", flatInfo);
			Template template = freeMarkerConfiguration.getTemplate("flat_email_template.html");
			
			StringWriter stringWriter = new StringWriter();
			template.process(input, stringWriter);
			return Optional.of(stringWriter.toString());

		} catch (IOException | TemplateException e) {
			LOGGER.debug("Could not fill in email template", e);
		}
		return Optional.empty();
	}

	private java.util.function.Consumer<String> addAddressToMessage(MimeMessage message) {
		return address -> {
			try {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		};
	}
}
