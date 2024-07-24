package telran.storehouse.service;

import telran.storehouse.dto.*;

public interface ContainerAndOrderProviderService {
	
	ContainerDataDto getContainer(long sensorId);
	long getOrderId(long containerId);

}
