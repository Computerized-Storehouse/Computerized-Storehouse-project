package telran.storehouse.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.storehouse.dto.ContainerDataDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerAndOrderProviderServiceImpl implements ContainerAndOrderProviderService {
	final RestTemplate restTemplate;
	final ServiceConfiguration serviceConfiguration;

	@Override
	public ContainerDataDto getContainer(long sensorId) {
		ContainerDataDto container= serviceRequestContainer(sensorId, "container");
		if(container.equals(null)) {
			log.debug("Container with sensor used ID {} not found", sensorId);
		}
		return container;
	}

	@Override
	public long getOrderId(long containerId) {
		long orderId = serviceRequestOrder(containerId, "order");
		if(orderId == 0) {
			log.debug("Order with container ID {} alredy exist", containerId);
		}
		return orderId;
	}
	
	private ContainerDataDto serviceRequestContainer(long id, String provider){
		ContainerDataDto container = null;
		ResponseEntity<?> responseEntity = restTemplate.exchange(getUrl(id, provider), HttpMethod.GET, null, ContainerDataDto.class);
		try {
			if(!responseEntity.getStatusCode().is2xxSuccessful()) {
				throw new Exception(responseEntity.getBody().toString());
			}
			container = (ContainerDataDto) responseEntity.getBody();
			log.debug("Recieved container {}", container.containerId());
        } catch (Exception e) {
            log.error("Error at service request: {}", e.getMessage());
		}
		return container;
		
	}
	
	private long serviceRequestOrder(long id, String provider){
		long value = 0;
		ResponseEntity<?> responseEntity = restTemplate.exchange(getUrl(id, provider), HttpMethod.GET, null, Long.class);
		try {
			if(!responseEntity.getStatusCode().is2xxSuccessful()) {
				throw new Exception(responseEntity.getBody().toString());
			}
			value = (long) responseEntity.getBody();
			log.debug("Order ID is {}", value);
        } catch (Exception e) {
            log.error("Error at service request: {}", e.getMessage());
		}
		return value;
		
	}

	private String getUrl(long id, String provider) {
		String url = "";
		if(provider.contains("order")) {
			url = String.format("http://%s:%d%s%d", serviceConfiguration.getOrderHost(), 
				serviceConfiguration.getOrderPort(), serviceConfiguration.orderPath, id);
		} else {
			url = String.format("http://%s:%d%s%d", serviceConfiguration.getContainerHost(), 
					serviceConfiguration.getContainerPort(), serviceConfiguration.getContainerPath(), id);
		}
		log.debug("url created is {}", url);
		return url;
	}

}
