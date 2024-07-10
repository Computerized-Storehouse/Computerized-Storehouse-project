package telran.storehouse.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinimumValueProviderServiceImpl implements MinimumValueProviderService {
	final RestTemplate restTemplate;
	final ServiceConfiguration serviceConfiguration;

	@Override
	public Double getValue(long sensorId) {
		double value = serviceRequest(sensorId);
		if(value == 0.0) {
			value = MinimumValueProviderService.DEFAULT_TRESHOLD_VALUE;
			log.debug("The threshold value is equal null, default value {}", value);
		}
		return value;
	}
	
	private Double serviceRequest(long sensorId){
		double value = 0;
		ResponseEntity<?> responseEntity = restTemplate.exchange(getUrl(sensorId), HttpMethod.GET, null, Double.class);
		try {
			if(!responseEntity.getStatusCode().is2xxSuccessful()) {
				throw new Exception(responseEntity.getBody().toString());
			}
			value = (double) responseEntity.getBody();
			log.debug("Thrshold value is {}", value);
        } catch (Exception e) {
            log.error("Error at service request: {}", e.getMessage());
		}
		return value;
		
	}

	private String getUrl(long sensorId) {
		String url = String.format("http://%s:%d%s%d", serviceConfiguration.getHost(), 
				serviceConfiguration.getPort(), serviceConfiguration.getPath(), sensorId);
		log.debug("url created is {}", url);
		return url;
	}

}
