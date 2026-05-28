# EventsApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createEvent**](EventsApi.md#createevent) | **POST** /api/events/create | Create event |
| [**deleteEvent**](EventsApi.md#deleteevent) | **DELETE** /api/events/delete/{id} | Delete event |
| [**getAllEvents**](EventsApi.md#getallevents) | **GET** /api/events/all | Get all events |
| [**getAllEventsForAdmin**](EventsApi.md#getalleventsforadmin) | **GET** /api/events/admin/all | Get all events for admin |
| [**getEventById**](EventsApi.md#geteventbyid) | **GET** /api/events/get/{id} | Get event by ID |
| [**setEventHidden**](EventsApi.md#seteventhidden) | **PATCH** /api/events/{id}/hidden | Set event hidden state |
| [**updateEvent**](EventsApi.md#updateevent) | **PUT** /api/events/update/{id} | Update event |



## createEvent

> EventResponse createEvent(eventCreateDTO)

Create event

Create a new event with sections and seats

### Example

```ts
import {
  Configuration,
  EventsApi,
} from '';
import type { CreateEventRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new EventsApi();

  const body = {
    // EventCreateDTO
    eventCreateDTO: ...,
  } satisfies CreateEventRequest;

  try {
    const data = await api.createEvent(body);
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
| **eventCreateDTO** | [EventCreateDTO](EventCreateDTO.md) |  | |

### Return type

[**EventResponse**](EventResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **503** | Service Unavailable |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## deleteEvent

> deleteEvent(id)

Delete event

Remove event from system

### Example

```ts
import {
  Configuration,
  EventsApi,
} from '';
import type { DeleteEventRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new EventsApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies DeleteEventRequest;

  try {
    const data = await api.deleteEvent(body);
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
| **id** | `string` |  | [Defaults to `undefined`] |

### Return type

`void` (Empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **503** | Service Unavailable |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getAllEvents

> Array&lt;EventResponse&gt; getAllEvents()

Get all events

Retrieve list of all available events

### Example

```ts
import {
  Configuration,
  EventsApi,
} from '';
import type { GetAllEventsRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new EventsApi();

  try {
    const data = await api.getAllEvents();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**Array&lt;EventResponse&gt;**](EventResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **503** | Service Unavailable |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getAllEventsForAdmin

> Array&lt;EventResponse&gt; getAllEventsForAdmin()

Get all events for admin

Retrieve list of all events, including hidden events

### Example

```ts
import {
  Configuration,
  EventsApi,
} from '';
import type { GetAllEventsForAdminRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new EventsApi();

  try {
    const data = await api.getAllEventsForAdmin();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**Array&lt;EventResponse&gt;**](EventResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **503** | Service Unavailable |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getEventById

> EventResponse getEventById(id)

Get event by ID

Retrieve event details by UUID

### Example

```ts
import {
  Configuration,
  EventsApi,
} from '';
import type { GetEventByIdRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new EventsApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GetEventByIdRequest;

  try {
    const data = await api.getEventById(body);
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
| **id** | `string` |  | [Defaults to `undefined`] |

### Return type

[**EventResponse**](EventResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **503** | Service Unavailable |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## setEventHidden

> EventResponse setEventHidden(id, hidden)

Set event hidden state

Hide or show an event in public listings

### Example

```ts
import {
  Configuration,
  EventsApi,
} from '';
import type { SetEventHiddenRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new EventsApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // boolean
    hidden: true,
  } satisfies SetEventHiddenRequest;

  try {
    const data = await api.setEventHidden(body);
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
| **id** | `string` |  | [Defaults to `undefined`] |
| **hidden** | `boolean` |  | [Defaults to `undefined`] |

### Return type

[**EventResponse**](EventResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **503** | Service Unavailable |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## updateEvent

> EventResponse updateEvent(id, eventCreateDTO)

Update event

Update existing event information

### Example

```ts
import {
  Configuration,
  EventsApi,
} from '';
import type { UpdateEventRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new EventsApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // EventCreateDTO
    eventCreateDTO: ...,
  } satisfies UpdateEventRequest;

  try {
    const data = await api.updateEvent(body);
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
| **id** | `string` |  | [Defaults to `undefined`] |
| **eventCreateDTO** | [EventCreateDTO](EventCreateDTO.md) |  | |

### Return type

[**EventResponse**](EventResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **503** | Service Unavailable |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

