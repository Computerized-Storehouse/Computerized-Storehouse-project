package telran.storehouse.service;

import telran.storehouse.dto.OrderDataDto;

public interface ReplenishmentDetectorService {
	
	double DEFAULT_TRESHOLD_VALUE = 30;
	
	OrderDataDto getOrder(long sensorId);

}
