package telran.storehouse;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.storehouse.dto.OrderDataDto;
import telran.storehouse.service.OrdersUpdaterService;
import org.springframework.cloud.stream.function.StreamBridge;
@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class OrdersUpdaterAppl {
	final OrdersUpdaterService service;
	final StreamBridge streamBridge;
	@Value("${app.orders.updater.producer.info.binding.name}")
	private String producerBindingName;
	
	public static void main(String[] args) {
		SpringApplication.run(OrdersUpdaterAppl.class, args);
	}
	@Bean
	Consumer<Long> ordersUpdaterConsumer(){
		return orderId -> orderUpdateProcessing(orderId);
	}
	private void orderUpdateProcessing(Long orderId) {
		OrderDataDto completedOrder = service.updateOrder(orderId);
		streamBridge.send(producerBindingName, completedOrder);
		log.debug("Order {} closed", completedOrder.orderId());
		
	}
}
