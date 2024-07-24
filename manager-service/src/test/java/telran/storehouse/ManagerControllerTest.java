package telran.storehouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.*;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import telran.storehouse.dto.*;
import telran.storehouse.service.ContainerAndOrderProviderService;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class ManagerControllerTest {
	private static final long SENSOR_ID = 123;
	private static final long SENSOR_ID_2 = 124;
	private static final long SENSOR_ID_3 = 125;
	private static final long CONTAINER_ID = 4578;
	private static final long CONTAINER_ID_2 = 3564;
	private static final long ORDER_ID = 135;
	private static final String COORDINATES = "A4";
	private static final double MAX_VALUE = 2400;
	private static final double CURRENT_VALUE = 70;
	private static final double THRESHOLD_VALUE = 30;
	private static final double FULLLNESS = 3;
	private static final long REQUARED_QUANTITY = 2330;
	@Autowired
	InputDestination producer;
	@Autowired
	OutputDestination consumer;
	@MockBean
	ContainerAndOrderProviderService providerService;
	
	@Test
	void loadApplicationContext() {
		assertNotNull(providerService);
	}
	
	@Value("${app.manager.service.producer.binding.name}")
	private String producerBindingName;
	private String consumerBindingName = "managerServiceConsumer-in-0";
	private SensorDataDto SENSOR_DATA = new SensorDataDto(SENSOR_ID, FULLLNESS, 0);
	private SensorDataDto SENSOR_DATA_2 = new SensorDataDto(SENSOR_ID_2, FULLLNESS, 0);
	private SensorDataDto SENSOR_DATA_NOT_EXIST = new SensorDataDto(SENSOR_ID_3, FULLLNESS, 0);
	private ContainerDataDto CONTAINER_DATA = new ContainerDataDto(CONTAINER_ID, SENSOR_ID, COORDINATES, MAX_VALUE, CURRENT_VALUE,
			ContainerStatus.OK, THRESHOLD_VALUE, null);
	private ContainerDataDto CONTAINER_DATA_2 = new ContainerDataDto(CONTAINER_ID_2, SENSOR_ID_2, COORDINATES, MAX_VALUE, CURRENT_VALUE,
			ContainerStatus.OK, THRESHOLD_VALUE, null);
	private OrderDataDto ORDER_DATA = new OrderDataDto(ORDER_ID, CONTAINER_ID, COORDINATES, null, REQUARED_QUANTITY, 0, 0, "auto", OrderStatus.OPEN);
	


	@BeforeEach
	void setUp() throws Exception {
		when(providerService.getContainer(SENSOR_ID)).thenReturn(CONTAINER_DATA);
		when(providerService.getOrderId(CONTAINER_ID)).thenReturn(ORDER_ID);
		when(providerService.getContainer(SENSOR_ID_2)).thenReturn(CONTAINER_DATA_2);
		
	}

	@Test
	void createOrder_sendData_test() throws Exception {
		producer.send(new GenericMessage<SensorDataDto>(SENSOR_DATA), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNotNull(message);
		ObjectMapper mapper = new ObjectMapper();
		OrderDataDto actual = mapper.readValue(message.getPayload(), OrderDataDto.class);
		OrderDataDto expected = ORDER_DATA;
		assertEquals(expected.orderId(), actual.orderId());
		assertEquals(expected.status(), actual.status());
			
	}
	
	@Test
	void orderStatusIsOpen_noGetOrderId_test() {
		producer.send(new GenericMessage<SensorDataDto>(SENSOR_DATA_2), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNull(message);
	}
	
	@Test
	void containerNotExist_noGetContainerData_test() {
		producer.send(new GenericMessage<SensorDataDto>(SENSOR_DATA_NOT_EXIST), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNull(message);
	}	

}
