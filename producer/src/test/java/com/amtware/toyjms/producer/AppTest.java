package com.amtware.toyjms.producer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class AppTest {

	
	@Test
	public void httpRequestorTest() {
//		WikipediaFacade requestor = new WikipediaFacade();
//		String json = requestor.request("word");
//		assertThat(json, equalTo(""));
	}
	
	/**
	 * Rigourous Test :-)
	 */
	@Test
	public void testApp() {
		assertThat(true, equalTo(true));
	}
}
