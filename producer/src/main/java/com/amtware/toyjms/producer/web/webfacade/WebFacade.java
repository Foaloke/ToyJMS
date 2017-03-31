package com.amtware.toyjms.producer.web.webfacade;

import java.util.List;
import java.util.stream.Stream;

public interface WebFacade {

	List<String> request(String word);
	Stream<String> getCommands();

}
