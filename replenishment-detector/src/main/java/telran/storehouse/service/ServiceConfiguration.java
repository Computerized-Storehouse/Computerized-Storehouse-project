package telran.storehouse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Configuration
@Getter
public class ServiceConfiguration {
	@Value("${app.order.provider..host:localhost}")
	String host;
	@Value("${app.order.provider.port}")
	int port;
	@Value("${app.order.provider.path}")
	String path;
	
	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
