package com.amtware.toyjms.storage;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.amtware.toyjms.storage.entity.Source;

public class AppTest {

	@Test
	public void testApp() {
		SessionFactory sessionFactory = new Configuration().configure()
				.buildSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
 
		Source user = new Source("label", "text");
		session.save(user);
 
		session.getTransaction().commit();
		session.close();
	}

}
