
# BloodLink - Timeout Budget

## 1. Objective

Define the maximum waiting time for communication between donor-service and the external services.

## 2. Timeout Configuration

| Communication | Timeout | Description |
|---|---:|---|
| gRPC | 2 seconds | Maximum duration for one attempt |
| gRPC Retry | 2 attempts + 300 ms pause | Maximum total duration: approximately 4.3 seconds |
| SOAP Connection | 1 second | Maximum time to establish the connection |
| SOAP Read | 3 seconds | Maximum time to receive the response |
| API Gateway | 10 seconds | Maximum time for the gateway to wait for donor-service |

## 3. Total Timeout Budget

- gRPC maximum duration: approximately 4.3 seconds.
- SOAP maximum duration: 4 seconds.
- Estimated total duration: 8.3 seconds.
- API Gateway timeout: 10 seconds.

The API Gateway timeout must be greater than the estimated donor-service processing time.

## 4. Failure Handling

- gRPC UNAVAILABLE: technical failure.
- gRPC DEADLINE_EXCEEDED: timeout failure.
- SOAP connection/read timeout: technical failure.
- Business errors (such as NOT_FOUND or rejected requests) must be handled as normal business responses, not as service outages.
