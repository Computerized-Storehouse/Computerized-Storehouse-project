package telran.storehouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import telran.storehouse.dto.OrderDataDto;
import telran.storehouse.dto.OrderStatus;
import telran.storehouse.dto.ProductDto;
import telran.storehouse.exceptions.IllegalOrderStateException;
import telran.storehouse.service.OrdersUpdaterService;

@Slf4j
@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class OrdersUpdaterControllerTest {
	
	private static final long ORDER_ID = 100;
	@MockBean
	OrdersUpdaterService service;
	@Autowired
	OutputDestination consumer;
    @Autowired
    InputDestination producer;
    
    @Value("${app.orders.updater.producer.info.binding.name}")
	private String producerBindingName;
    
    private String consumerBindingName = "ordersUpdaterConsumer-in-0";
   
    final ProductDto product = new ProductDto("Product", "Units");
	final OrderDataDto orderDto = new OrderDataDto(ORDER_ID, 4321L, "A123", product, System.currentTimeMillis(),
			System.currentTimeMillis(), 20L, "creator", OrderStatus.CLOSE);
    @BeforeEach
    void setUp() {
    	when(service.updateOrder(ORDER_ID)).thenReturn(orderDto);
    }
    @Test
    void orderUpdaterControllerTest() throws StreamReadException, DatabindException, IOException {
    	try {
    		producer.send(new GenericMessage<>(ORDER_ID), consumerBindingName);
    		log.debug("send:{}", ORDER_ID);
    	}catch (IllegalOrderStateException e) {
    		assertEquals("Order already exist", e.getMessage());
    	}
    	Message<byte[]> message = consumer.receive(10, producerBindingName);
		log.debug("receive:{}", message);
		assertNotNull(message, "Expected message to be non-null");
		ObjectMapper mapper = new ObjectMapper();
		OrderDataDto actual = mapper.readValue(message.getPayload(), OrderDataDto.class);
		log.debug("actual {}", actual);
		assertEquals(ORDER_ID, actual.orderId());
    }
}