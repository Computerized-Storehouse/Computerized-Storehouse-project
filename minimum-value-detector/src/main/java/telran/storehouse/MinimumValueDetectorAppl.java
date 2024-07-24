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
import telran.storehouse.service.MinimumValueProviderService;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class MinimumValueDetectorAppl {
	@Value("${app.minimum.value.detector.producer.binding.name}")
	private String producerBindingName;
	final MinimumValueProviderService valueProviderService;
	final StreamBridge streamBridge;

	public static void main(String[] args) {

		SpringApplication.run(MinimumValueDetectorAppl.class, args);
	}

	@Bean
	Consumer<NewStateDto> valueDetectorConsumer() {

		return this::lackDetectorProcessing;
	}

	void lackDetectorProcessing(NewStateDto newStateDto) {
		double difference = newStateDto.difference();
		if (difference > 0) {
			log.trace("received new state sensor data {} in the lack detector", newStateDto);
			SensorDataDto sensorData = newStateDto.sensorData();
			long sensorId = sensorData.id();
			double fullness = sensorData.fullness();
			double thresholdValue = valueProviderService.getValue(sensorId);
			if (thresholdValue > fullness) {
				streamBridge.send(producerBindingName, sensorData);
				log.debug("value of sensor {} is less than threshold, data has been sent to {} binding name", sensorId,
						producerBindingName);
			} else {
				log.trace("senosor {} has value greater than threshold value", sensorId);
			}
		}
	}

}
