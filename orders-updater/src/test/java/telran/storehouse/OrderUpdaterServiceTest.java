package telran.storehouse;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import telran.storehouse.dto.OrderDataDto;
import telran.storehouse.dto.OrderStatus;
import telran.storehouse.dto.ProductDto;
import telran.storehouse.entity.CompletedOrder;
import telran.storehouse.entity.Order;
import telran.storehouse.entity.Product;
import telran.storehouse.exceptions.IllegalOrderStateException;
import telran.storehouse.exceptions.OrderNotFoundException;
import telran.storehouse.repo.CompletedOrdersRepo;
import telran.storehouse.repo.OrdersRepo;
import telran.storehouse.service.*;

@SpringBootTest
@Sql(scripts = { "classpath:test_data.sql" })
class OrderUpdaterServiceTest {
	private static final long ORDER_ID_NORMAL = 100;
	private static final long ORDER_ID_NOT_EXIST = 200;
	private static final long COMPLETED_ORDER_ID_NORMAL = 200;
	@Autowired
	private OrdersUpdaterService ordersUpdaterService;
	@Autowired
	private OrdersRepo ordersRepo;
	@Autowired
	private CompletedOrdersRepo completedOrdersRepo;

	final ProductDto product1 = new ProductDto("product1", "unit1");
	final OrderDataDto orderDtoCopy = new OrderDataDto(100, 300, "A1", product1, 30, 2023 - 06 - 06, 2023 - 07 - 07,
			"creator1", OrderStatus.OPEN);
	final OrderDataDto completedOrderDto = new OrderDataDto(COMPLETED_ORDER_ID_NORMAL, 300, "A1", product1, 30,
			2023 - 06 - 06, 2023 - 07 - 07, "creator1", OrderStatus.CLOSE);

	@Test
	void loadContext() {
		assertNotNull(ordersUpdaterService);
	}

	@Transactional
	@Rollback
	@Test
	void orderUpdater_updateOrder() {
		assertEquals(ORDER_ID_NORMAL, ordersUpdaterService.updateOrder(ORDER_ID_NORMAL).orderId());
		List<Order> listOrders = ordersRepo.findAll();
		assertEquals(2, listOrders.size());
		List<CompletedOrder> listCompletedOrders = completedOrdersRepo.findAll();
		assertEquals(1, listCompletedOrders.size());
		Product product = Product.of(product1);
		Order order = Order.of(completedOrderDto);
		order.setProduct(product);
		completedOrdersRepo.save(CompletedOrder.of(order));
		Optional<CompletedOrder> completedOrder = completedOrdersRepo.findById(COMPLETED_ORDER_ID_NORMAL);
		assertEquals(COMPLETED_ORDER_ID_NORMAL, completedOrder.get().getOrderId());
		assertEquals(OrderStatus.CLOSE, completedOrder.get().getStatus());

	}

	@Test
	void orderUpdater_Order_not_found() {
		assertThrowsExactly(OrderNotFoundException.class, () -> ordersUpdaterService.updateOrder(ORDER_ID_NOT_EXIST));
	}

	@Test
	void orderUpdater_add_CompletedOrder_existing_id() {
		ordersUpdaterService.updateOrder(ORDER_ID_NORMAL);
		Product product = Product.of(product1);
		Order order = Order.of(orderDtoCopy);
		order.setProduct(product);
		ordersRepo.save(order);
		assertThrowsExactly(IllegalOrderStateException.class, () -> ordersUpdaterService.updateOrder(ORDER_ID_NORMAL));
	}
}