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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.*;

import telran.storehouse.dto.*;
import telran.storehouse.service.ReplenishmentDetectorService;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class ReplenishmentDetectorServiceTest {

	private static final String URL = "http://localhost:8484/orders/data/";
	private static final long SENSOR_ID = 123;
	private static final long ORDER_ID_1 = 45;
	private static final long ORDER_NOT_FOUND_BY_SENSOR_ID = 125;
	
	@Autowired
	InputDestination producer;
	@Autowired
	ReplenishmentDetectorService detectorService;
	@MockBean
	RestTemplate restTemplate;

	private ProductDto productData = new ProductDto("product1", "unit1");
	private OrderDataDto ORDER_NORMAL = new OrderDataDto(ORDER_ID_1, SENSOR_ID, "A4", productData, 150, 0, 0, "AUTHOMATIC", OrderStatus.OPEN);;

	@Test
	void normalFlow_returnsOrderDataDto() {
		ResponseEntity<OrderDataDto> responseEntity = new ResponseEntity<>(ORDER_NORMAL , HttpStatus.OK);
		when(restTemplate.exchange(getUrl(SENSOR_ID), HttpMethod.GET, null, OrderDataDto.class))
		.thenReturn(responseEntity);
		assertEquals(ORDER_NORMAL.orderId(), detectorService.getOrder(SENSOR_ID).orderId());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void remoteWebServiceUnavailable() {
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
		.thenThrow(new RuntimeException("Service is unavailable"));
		
	}
	
	@Test
	void orderNotFound_BySensorId_Test() {
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(OrderDataDto.class)))
		.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

		Exception exception = assertThrows(Exception.class, () -> {
			detectorService.getOrder(ORDER_NOT_FOUND_BY_SENSOR_ID);
        });

        assertEquals("404 NOT_FOUND", exception.getMessage());
	}
	
	private String getUrl(long sensorId) {
		
		return URL + sensorId;
	}


}
