package telran.storehouse.service;

import telran.storehouse.dto.OrderDataDto;

public interface CWAgentService {
	void sendLogOrder(OrderDataDto orderData);
}
