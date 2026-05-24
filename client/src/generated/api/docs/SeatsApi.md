# SeatsApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getSeatsByEvent**](SeatsApi.md#getseatsbyevent) | **GET** /api/seats/event/{eventId} |  |
| [**getSeatsBySection**](SeatsApi.md#getseatsbysection) | **GET** /api/seats/section/{sectionId} |  |



## getSeatsByEvent

> Array&lt;SeatResponse&gt; getSeatsByEvent(eventId)



### Example

```ts
import {
  Configuration,
  SeatsApi,
} from '';
import type { GetSeatsByEventRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SeatsApi();

  const body = {
    // string
    eventId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GetSeatsByEventRequest;

  try {
    const data = await api.getSeatsByEvent(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **eventId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**Array&lt;SeatResponse&gt;**](SeatResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **500** | Internal Server Error |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getSeatsBySection

> Array&lt;SeatResponse&gt; getSeatsBySection(sectionId)



### Example

```ts
import {
  Configuration,
  SeatsApi,
} from '';
import type { GetSeatsBySectionRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SeatsApi();

  const body = {
    // string
    sectionId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GetSeatsBySectionRequest;

  try {
    const data = await api.getSeatsBySection(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **sectionId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**Array&lt;SeatResponse&gt;**](SeatResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **500** | Internal Server Error |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

