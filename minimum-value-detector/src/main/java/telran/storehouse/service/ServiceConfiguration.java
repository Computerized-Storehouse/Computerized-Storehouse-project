package telran.storehouse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Configuration
@Getter
public class ServiceConfiguration {
	@Value("${app.threshold.provider.host:localhost}")
	String host;
	@Value("${app.threshold.provider.port}")
	int port;
	@Value("${app.threshold.provider.path}")
	String path;
	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
