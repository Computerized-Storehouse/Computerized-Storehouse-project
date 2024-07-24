package telran.storehouse;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.storehouse.dto.*;
import telran.storehouse.service.ContainerAndOrderProviderService;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class ManagerServiceAppl {
	@Value("${app.manager.service.producer.binding.name}")
	private String producerBindingName;
	final ContainerAndOrderProviderService service;
	final StreamBridge streamBridge;

	public static void main(String[] args) {
		SpringApplication.run(ManagerServiceAppl.class, args);

	}
	
	@Bean
	Consumer<SensorDataDto> managerServiceConsumer(){
		return this::managerServiceProcessing;
	}
	
	void managerServiceProcessing(SensorDataDto sensorData) {
			log.trace("received sensor data {} in the manager service", sensorData);
			ContainerDataDto container = service.getContainer(sensorData.id());
			if(!container.equals(null)) {
				long containerId = container.containerId();
				log.debug("received container data {} by sensor ID", containerId, sensorData.id());
				long orderId = service.getOrderId(containerId);
				if(orderId  != 0) {
					long requiredQuantity = (long) (container.containerMaxValue() * (1 - container.containerCurrentValue() / 100));
					OrderDataDto order = new OrderDataDto(orderId, containerId, container.coordinates(), container.product(), 
							requiredQuantity, System.currentTimeMillis(), 0, "AUTO", OrderStatus.OPEN);
					log.debug("create order {}", orderId);
					streamBridge.send(producerBindingName, order);
					log.debug("order {} data has been sent to {} binding name", orderId, producerBindingName);
				}
			}
		
	}

}
