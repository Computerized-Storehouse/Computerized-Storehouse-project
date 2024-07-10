package telran.storehouse.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.storehouse.dto.OrderDataDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplenishmentDetectorServiceImpl implements ReplenishmentDetectorService {
	final RestTemplate restTemplate;
	final ServiceConfiguration serviceConfiguration;

	@Override
	public OrderDataDto getOrder(long sensorId) {
		OrderDataDto order = serviceRequest(sensorId);
		if(order.equals(null)) {
			log.debug("Order by sensor ID {} is empty", sensorId);
		}
		return order;
	}

	private OrderDataDto serviceRequest(long sensorId) {
		ResponseEntity<?> responseEntity = restTemplate.exchange(getUrl(sensorId), HttpMethod.GET, null, OrderDataDto.class);
		OrderDataDto order = null;
		try {
			if(!responseEntity.getStatusCode().is2xxSuccessful()) {
				throw new Exception(responseEntity.getBody().toString());
			}
			order = (OrderDataDto) responseEntity.getBody();
			log.debug("Order is {}", order);
        } catch (Exception e) {
            log.error("Error at service request: {}", e.getMessage());
		}
		return order;
	}

	private String getUrl(long sensorId) {
		String url = String.format("http://%s:%d%s%d", serviceConfiguration.getHost(), 
				serviceConfiguration.getPort(), serviceConfiguration.getPath(), sensorId);
		log.debug("url created is {}", url);
		return url;
	}

}
