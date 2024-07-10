package telran.storehouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import telran.storehouse.service.MinimumValueProviderService;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MinimumValueDetectorServiceTest {
	private static final long SENSOR_ID = 123;
	private static final double VALUE = 40;
	private static final String URL = "http://localhost:8282/threshold/";
	private static final long SENSOR_ID_NOT_FOUND = 124;
	private static final double VALUE_DEFAULT = MinimumValueProviderService.DEFAULT_TRESHOLD_VALUE;

	@Autowired
	InputDestination producer;
	@Autowired
	MinimumValueProviderService valueProviderService;
	@MockBean
	RestTemplate restTemplate;


	@Test
	@Order(1)
	void normalFlow_returnsThreshold() {
		ResponseEntity<Double> responseEntity = new ResponseEntity<>(VALUE, HttpStatus.OK);
		when(restTemplate.exchange(getUrl(SENSOR_ID), HttpMethod.GET, null, Double.class))
		.thenReturn(responseEntity);
		assertEquals(VALUE, valueProviderService.getValue(SENSOR_ID));
	}
	
	
	@Test
	@Order(2)
	void sensorNotFoundTest() {
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(Double.class)))
		.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

		Exception exception = assertThrows(Exception.class, () -> {
            valueProviderService.getValue(SENSOR_ID_NOT_FOUND);
        });

        assertEquals("404 NOT_FOUND", exception.getMessage());
	}
	
	@Test
	@Order(3)
	void defaultValueNotInCache() {
		ResponseEntity<Double> responseEntity = new ResponseEntity<>(0.0 , HttpStatus.OK);
		when(restTemplate.exchange(getUrl(SENSOR_ID), HttpMethod.GET, null, Double.class))
		.thenReturn(responseEntity);
		assertEquals(VALUE_DEFAULT, valueProviderService.getValue(SENSOR_ID));
	}
	@SuppressWarnings("unchecked")
	@Test
	void remoteWebServiceUnavailable() {
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
		.thenThrow(new RuntimeException("Service is unavailable"));
		
	}

	private String getUrl(long sensorId) {
		
		return URL + sensorId;
	}

}
