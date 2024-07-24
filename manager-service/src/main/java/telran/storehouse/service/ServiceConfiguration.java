package telran.storehouse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Configuration
@Getter
public class ServiceConfiguration {
	@Value("${app.container.provider.host:localhost}")
	String containerHost;
	@Value("${app.container.provider.port}")
	int containerPort;
	@Value("${app.container.provider.path}")
	String containerPath;
	@Value("${app.order.provider.host:localhost}")
	String orderHost;
	@Value("${app.order.provider.port}")
	int orderPort;
	@Value("${app.order.provider.path}")
	String orderPath;
	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
