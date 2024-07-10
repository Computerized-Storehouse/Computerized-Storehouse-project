package telran.storehouse.service;

import java.time.*;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import telran.storehouse.dto.*;
@Service
@Slf4j
@RequiredArgsConstructor
public class CWAgentServiceImpl implements CWAgentService {
	@Value("${app.aws.cloudwatch.logs.group.name}")
	String logGroupName;
	@Value("${app.aws.cloudwatch.logs.stream.name}")
	String streamName;
	@Value("${app.aws.region:us-east-1}")
    private String region;
	CloudWatchLogsClient logsClient;
	DescribeLogStreamsRequest logStreamRequest;
	DescribeLogStreamsResponse describeLogStreamsResponse;
	String sequenceToken;

	@Override
	public void sendLogOrder(OrderDataDto orderData) {
		try {
		InputLogEvent inputLogEvent = InputLogEvent.builder().message(getMessage(orderData))
				.timestamp(System.currentTimeMillis()).build();
		PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
				.logEvents(Arrays.asList(inputLogEvent)).logGroupName(logGroupName).logStreamName(streamName)
				.sequenceToken(sequenceToken).build();
		PutLogEventsResponse putLogEventsResponse = logsClient.putLogEvents(putLogEventsRequest);
        sequenceToken = putLogEventsResponse.nextSequenceToken();
        log.info("Successfully sent log event to CloudWatch: {}", getMessage(orderData));
		 } catch (InvalidSequenceTokenException e) {
	            log.error("Invalid sequence token provided: {}", e.getMessage());
	            refreshSequenceToken();
	            sendLogOrder(orderData);
    } catch (Exception e) {
    	log.error("Failed to send log event to CloudWatch: {}", e.getMessage(), e);
		}
	}
	private String getMessage(OrderDataDto orderData) {
		return orderData.status() == OrderStatus.OPEN 
				? String.format("Order %d added at %s", orderData.orderId(), getDateTime(orderData.openingTime()))
			            : String.format("Order %d closed at %s", orderData.orderId(), getDateTime(orderData.closingTime()));
	}
	private LocalDateTime getDateTime(long timestamp) {
		return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}
	@PostConstruct
	public
	void setCloudWatchLogs() {
		logsClient = CloudWatchLogsClient.builder().region(Region.of(region)).build();
		 logStreamRequest = DescribeLogStreamsRequest.builder().logGroupName(logGroupName)
				.logStreamNamePrefix(streamName).build();
		 describeLogStreamsResponse = logsClient.describeLogStreams(logStreamRequest);
		 if (!describeLogStreamsResponse.logStreams().isEmpty()) {
	            sequenceToken = describeLogStreamsResponse.logStreams().get(0).uploadSequenceToken();
	        } else {
	        	createLogStream();
	        }
	}
	 private void createLogStream() {
	        try {
	            logsClient.createLogStream(CreateLogStreamRequest.builder()
	                    .logGroupName(logGroupName)
	                    .logStreamName(streamName)
	                    .build());
	            log.info("Created new log stream: {}", streamName);
	            refreshSequenceToken();
	        } catch (Exception e) {
	            log.error("Failed to create log stream: {}", e.getMessage(), e);
	            throw new IllegalStateException("Failed to create log stream: " + streamName, e);
	        }
	    }
	 private void refreshSequenceToken() {
	        DescribeLogStreamsRequest logStreamRequest = DescribeLogStreamsRequest.builder()
	                .logGroupName(logGroupName)
	                .logStreamNamePrefix(streamName)
	                .build();

	        DescribeLogStreamsResponse describeLogStreamsResponse = logsClient.describeLogStreams(logStreamRequest);
	        if (!describeLogStreamsResponse.logStreams().isEmpty()) {
	            sequenceToken = describeLogStreamsResponse.logStreams().get(0).uploadSequenceToken();
	        } else {
	            throw new IllegalStateException("Log stream not found: " + streamName);
	        }
	    }
}
