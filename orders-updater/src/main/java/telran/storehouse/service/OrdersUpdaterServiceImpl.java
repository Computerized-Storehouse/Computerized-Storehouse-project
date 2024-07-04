package telran.storehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.storehouse.dto.OrderDataDto;
import telran.storehouse.dto.OrderStatus;
import telran.storehouse.entity.CompletedOrder;
import telran.storehouse.entity.Order;
import telran.storehouse.exceptions.*;
import telran.storehouse.repo.CompletedOrdersRepo;
import telran.storehouse.repo.OrdersRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersUpdaterServiceImpl implements OrdersUpdaterService {
	final OrdersRepo orderRepo;
	final CompletedOrdersRepo completedOrderRepo;

	@Override
	@Transactional
	public OrderDataDto updateOrder(long orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new OrderNotFoundException());
		CompletedOrder completedOrder = CompletedOrder.of(order);
		orderRepo.deleteById(orderId);
		log.debug("Order id {} deleted ", order.getOrderId());
		if (completedOrderRepo.existsById(orderId)) {
			throw new IllegalOrderStateException();

		}
		completedOrder.setStatus(OrderStatus.CLOSE);
		completedOrderRepo.save(completedOrder);
		log.debug("Completed order id {} has been saved", completedOrder.getOrderId());
		return completedOrder.build();
	}

}
