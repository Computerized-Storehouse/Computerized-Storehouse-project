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
import telran.storehouse.service.ReplenishmentDetectorService;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class ReplenishmentDetectorControllerTest {
	private static final long SENSOR_ID_1 = 123;
	private static final long SENSOR_ID_3 = 125;
	private static final double SENSOR_FULLNES_1 = 50;
	private static final double SENSOR_FULLNES_2 = 20;
	private static final long ORDER_ID_1 = 245;
	private static final long ORDER_ID_2 = 345;
	@Autowired
	InputDestination producer;
	@Autowired
	OutputDestination consumer;
	@MockBean
	ReplenishmentDetectorService detectorService;
	
	@Test
	void loadApplicationContext() {
		assertNotNull(detectorService);
	}
	
	@Value("${app.replenishment.detector.producer.binding.name}")
	private String producerBindingName;
	private String consumerBindingName = "replenishmentDetectorConsumer-in-0";
	private ProductDto productData = new ProductDto("product1", "unit1");
	private OrderDataDto ORDER_DATA_OPEN = new OrderDataDto(ORDER_ID_1, SENSOR_ID_1, "A4", productData, 150, 0, 0, "AUTHOMATIC", OrderStatus.OPEN);
	private OrderDataDto ORDER_DATA_CLOSE = new OrderDataDto(ORDER_ID_2, SENSOR_ID_3, "B4", productData, 150, 0, 0, "AUTHOMATIC", OrderStatus.CLOSE);
	private SensorDataDto sensorDataWithGreaterValue = new SensorDataDto(SENSOR_ID_1, SENSOR_FULLNES_1, 0);
	private SensorDataDto sensorDataWithGreaterValueOrderClose = new SensorDataDto(SENSOR_ID_3, SENSOR_FULLNES_1, 0);
	private NewStateDto newStateWithSensorDataGreaterThresholdValue = new NewStateDto(sensorDataWithGreaterValue, -30);
	private SensorDataDto sensorDataWithLessValue = new SensorDataDto(SENSOR_ID_1, SENSOR_FULLNES_2, 0);
	private NewStateDto newStateWithSensorDataLessThresholdValue = new NewStateDto(sensorDataWithLessValue, -10);
	private NewStateDto newStatewithDifferenceGreaterNull = new NewStateDto(sensorDataWithGreaterValue, 30);
	private NewStateDto newStateButOrderStatusIsClose = new NewStateDto(sensorDataWithGreaterValueOrderClose, -30);

	@BeforeEach
	void setUp(){
		when(detectorService.getOrder(SENSOR_ID_1)).thenReturn(ORDER_DATA_OPEN);
		when(detectorService.getOrder(SENSOR_ID_3)).thenReturn(ORDER_DATA_CLOSE);
	}

	@Test
	void application_createRequest_statusIsOpen_fullnessGreaterThanThreshold_sendData_Test() throws Exception {
		producer.send(new GenericMessage<NewStateDto>(newStateWithSensorDataGreaterThresholdValue), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNotNull(message);
		ObjectMapper mapper = new ObjectMapper();
		Long actual = mapper.readValue(message.getPayload(), Long.class);
		Long expected = ORDER_ID_1;
		assertEquals(expected, actual);
		
	}
	
	@Test
	void application_createRequest_statusIsOpen_fullnessLessThanThreshold_notSendData_Test() {
		producer.send(new GenericMessage<NewStateDto>(newStateWithSensorDataLessThresholdValue ), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNull(message);
	}
	
	@Test
	void application_createRequest_statusIsClosed_notSendData_Test() {
		producer.send(new GenericMessage<NewStateDto>(newStateButOrderStatusIsClose), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNull(message);
	}
	
	
	@Test
	void application_noCreateRequest_DifferenceGreaterNull_Test() {
		producer.send(new GenericMessage<NewStateDto>(newStatewithDifferenceGreaterNull), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNull(message);
	}

}
