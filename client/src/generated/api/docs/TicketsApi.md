# TicketsApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createTicket**](TicketsApi.md#createticket) | **POST** /api/tickets/create | Create ticket |
| [**deleteTicket**](TicketsApi.md#deleteticket) | **DELETE** /api/tickets/{id} | Cancel ticket |
| [**getAllTickets**](TicketsApi.md#getalltickets) | **GET** /api/tickets/all | Get all tickets |
| [**getMyTickets**](TicketsApi.md#getmytickets) | **GET** /api/tickets/my-tickets | Get my tickets |
| [**getTicketById**](TicketsApi.md#getticketbyid) | **GET** /api/tickets/{id} | Get ticket details |



## createTicket

> TicketResponse createTicket(ticketCreateDTO)

Create ticket

Create ticket directly (bypassing payment)

### Example

```ts
import {
  Configuration,
  TicketsApi,
} from '';
import type { CreateTicketRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new TicketsApi();

  const body = {
    // TicketCreateDTO
    ticketCreateDTO: ...,
  } satisfies CreateTicketRequest;

  try {
    const data = await api.createTicket(body);
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
| **ticketCreateDTO** | [TicketCreateDTO](TicketCreateDTO.md) |  | |

### Return type

[**TicketResponse**](TicketResponse.md)

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


## deleteTicket

> string deleteTicket(id)

Cancel ticket

Delete/cancel user\&#39;s ticket

### Example

```ts
import {
  Configuration,
  TicketsApi,
} from '';
import type { DeleteTicketRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new TicketsApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies DeleteTicketRequest;

  try {
    const data = await api.deleteTicket(body);
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

**string**

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


## getAllTickets

> Array&lt;TicketResponse&gt; getAllTickets()

Get all tickets

Retrieve all tickets in system (Admin only)

### Example

```ts
import {
  Configuration,
  TicketsApi,
} from '';
import type { GetAllTicketsRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new TicketsApi();

  try {
    const data = await api.getAllTickets();
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

[**Array&lt;TicketResponse&gt;**](TicketResponse.md)

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


## getMyTickets

> Array&lt;TicketResponse&gt; getMyTickets()

Get my tickets

Retrieve all tickets for authenticated user

### Example

```ts
import {
  Configuration,
  TicketsApi,
} from '';
import type { GetMyTicketsRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new TicketsApi();

  try {
    const data = await api.getMyTickets();
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

[**Array&lt;TicketResponse&gt;**](TicketResponse.md)

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


## getTicketById

> TicketDetailResponse getTicketById(id)

Get ticket details

Retrieve detailed ticket information including QR code

### Example

```ts
import {
  Configuration,
  TicketsApi,
} from '';
import type { GetTicketByIdRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new TicketsApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GetTicketByIdRequest;

  try {
    const data = await api.getTicketById(body);
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

[**TicketDetailResponse**](TicketDetailResponse.md)

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

