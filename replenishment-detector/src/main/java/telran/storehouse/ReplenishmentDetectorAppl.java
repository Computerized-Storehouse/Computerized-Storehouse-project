package telran.storehouse;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import telran.storehouse.dto.*;
import telran.storehouse.service.ReplenishmentDetectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ReplenishmentDetectorAppl {
	@Value("${app.replenishment.detector.producer.binding.name}")
	private String producerBindingName;
	final ReplenishmentDetectorService detectorService;
	final StreamBridge streamBridge;
	

	public static void main(String[] args) {
		SpringApplication.run(ReplenishmentDetectorAppl.class, args);

	}
	
	@Bean
	Consumer<NewStateDto> replenishmentDetectorConsumer(){
		return this::fullDetectorProcessing;
	}
	
	void fullDetectorProcessing(NewStateDto newStateDto) {
		double difference = newStateDto.difference();
		if(difference < 0) {
			log.trace("received new state sensor data {} in the full detector", newStateDto);
			SensorDataDto sensor = newStateDto.sensorData();
			long sensorId = sensor.id();
			OrderDataDto order = detectorService.getOrder(sensorId);
			long orderId = order.orderId();
			OrderStatus status = order.status();
			if(status.equals(OrderStatus.OPEN)) {
				log.debug("Order {} status is Open", orderId);
				if(sensor.fullness() > detectorService.DEFAULT_TRESHOLD_VALUE) {
					streamBridge.send(producerBindingName, orderId);
					log.debug("order {} data has been sent to {} binding name", orderId, producerBindingName);
				} else {
					log.trace("Sensor fullness {} has become less than default threshold value", sensor.fullness());
				}
			} else {
				log.trace("The container's current load has become greater, but the order {} status is already closed", orderId);
			}
			
		}
	}

}
