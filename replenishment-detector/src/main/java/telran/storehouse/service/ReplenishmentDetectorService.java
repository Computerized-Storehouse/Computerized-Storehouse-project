package telran.storehouse.service;

import telran.storehouse.dto.OrderDataDto;

public interface ReplenishmentDetectorService {
	
	OrderDataDto getOrder(long sensorId);

}
