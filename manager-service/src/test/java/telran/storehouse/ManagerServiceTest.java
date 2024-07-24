package telran.storehouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import telran.storehouse.dto.*;
import telran.storehouse.service.ContainerAndOrderProviderService;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class ManagerServiceTest {
	
	private static final String URL_CONTAINER = "http://localhost:8686/containers/data/";
	private static final String URL_ORDER = "http://localhost:8484/orders/data/";
	private static final long SENSOR_ID = 123;
	private static final long ORDER_ID = 123;
	private static final long CONTAINER_ID = 123;
	private static final long SENSOR_ID_NOT_FOUND = 345;
	
	@Autowired
	InputDestination producer;
	@Autowired
	ContainerAndOrderProviderService service;
	@MockBean
	RestTemplate restTemplate;
	
	private ContainerDataDto CONTAINER_DATA = new ContainerDataDto(CONTAINER_ID, SENSOR_ID, "A4", 2400, 20, ContainerStatus.OK, 30, null);

	@Test
	void normalFlow_returnsContainer() {
		ResponseEntity<ContainerDataDto> responseEntity = new ResponseEntity<>(CONTAINER_DATA, HttpStatus.OK);
		when(restTemplate.exchange(getUrl(SENSOR_ID, URL_CONTAINER), HttpMethod.GET, null, ContainerDataDto.class))
		.thenReturn(responseEntity);
		ContainerDataDto container = service.getContainer(SENSOR_ID);
		assertNotNull(container);
		assertEquals(SENSOR_ID, container.sensorUsedId());
	}
	
	@Test
	void normalFlow_returnsOrderId() {
		ResponseEntity<Long> responseEntity = new ResponseEntity<>(ORDER_ID, HttpStatus.OK);
		when(restTemplate.exchange(getUrl(CONTAINER_ID, URL_ORDER), HttpMethod.GET, null, Long.class))
		.thenReturn(responseEntity);
		assertEquals(ORDER_ID, service.getOrderId(CONTAINER_ID));
	}
	
	@Test
	@Order(2)
	void containerNotFoundTest() {
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(ContainerDataDto.class)))
		.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

		Exception exception = assertThrows(Exception.class, () -> {
            service.getContainer(SENSOR_ID_NOT_FOUND);
        });

        assertEquals("404 NOT_FOUND", exception.getMessage());
	}
	
	@Test
	@Order(2)
	void orderAlredyExistTest() {
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(Long.class)))
		.thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

		Exception exception = assertThrows(Exception.class, () -> {
			service.getOrderId(CONTAINER_ID);
        });

        assertEquals("409 CONFLICT", exception.getMessage());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void remoteWebServiceUnavailable() {
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
		.thenThrow(new RuntimeException("Service is unavailable"));
		
	}

	
	private String getUrl(long id, String url) {
		
		return url + id;
	}

}
