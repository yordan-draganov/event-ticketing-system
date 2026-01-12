# PaymentsApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**cancelPayment**](PaymentsApi.md#cancelpayment) | **POST** /api/payments/cancel/{paymentIntentId} | Cancel payment |
| [**confirmPayment**](PaymentsApi.md#confirmpayment) | **POST** /api/payments/confirm | Confirm payment |
| [**createPaymentIntent**](PaymentsApi.md#createpaymentintent) | **POST** /api/payments/create-intent | Create payment intent |
| [**getPaymentStatus**](PaymentsApi.md#getpaymentstatus) | **GET** /api/payments/status/{paymentIntentId} | Get payment status |



## cancelPayment

> string cancelPayment(paymentIntentId)

Cancel payment

Cancel pending payment intent

### Example

```ts
import {
  Configuration,
  PaymentsApi,
} from '';
import type { CancelPaymentRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: BearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new PaymentsApi(config);

  const body = {
    // string
    paymentIntentId: paymentIntentId_example,
  } satisfies CancelPaymentRequest;

  try {
    const data = await api.cancelPayment(body);
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
| **paymentIntentId** | `string` |  | [Defaults to `undefined`] |

### Return type

**string**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## confirmPayment

> object confirmPayment(paymentConfirmDTO)

Confirm payment

Verify payment success and retrieve/create ticket

### Example

```ts
import {
  Configuration,
  PaymentsApi,
} from '';
import type { ConfirmPaymentRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: BearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new PaymentsApi(config);

  const body = {
    // PaymentConfirmDTO
    paymentConfirmDTO: ...,
  } satisfies ConfirmPaymentRequest;

  try {
    const data = await api.confirmPayment(body);
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
| **paymentConfirmDTO** | [PaymentConfirmDTO](PaymentConfirmDTO.md) |  | |

### Return type

**object**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## createPaymentIntent

> PaymentResponse createPaymentIntent(paymentDTO)

Create payment intent

Initialize Stripe payment for selected seats

### Example

```ts
import {
  Configuration,
  PaymentsApi,
} from '';
import type { CreatePaymentIntentRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: BearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new PaymentsApi(config);

  const body = {
    // PaymentDTO
    paymentDTO: ...,
  } satisfies CreatePaymentIntentRequest;

  try {
    const data = await api.createPaymentIntent(body);
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
| **paymentDTO** | [PaymentDTO](PaymentDTO.md) |  | |

### Return type

[**PaymentResponse**](PaymentResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getPaymentStatus

> PaymentStatusResponse getPaymentStatus(paymentIntentId)

Get payment status

Check current status of payment intent

### Example

```ts
import {
  Configuration,
  PaymentsApi,
} from '';
import type { GetPaymentStatusRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: BearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new PaymentsApi(config);

  const body = {
    // string
    paymentIntentId: paymentIntentId_example,
  } satisfies GetPaymentStatusRequest;

  try {
    const data = await api.getPaymentStatus(body);
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
| **paymentIntentId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**PaymentStatusResponse**](PaymentStatusResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

