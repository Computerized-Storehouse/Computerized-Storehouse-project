package telran.storehouse;

import java.util.function.Consumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import lombok.RequiredArgsConstructor;
import telran.storehouse.dto.OrderDataDto;
import telran.storehouse.service.CWAgentService;

@SpringBootApplication
@RequiredArgsConstructor

public class CWAgentAppl {
	final CWAgentService service;

	
	public static void main(String[] args) {
		SpringApplication.run(CWAgentAppl.class, args);

	}
	@Bean
	Consumer<OrderDataDto> CWLogOrdersConsumer(){
		return service :: sendLogOrder;
	}
	
}
