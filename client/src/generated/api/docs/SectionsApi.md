# SectionsApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getSectionById**](SectionsApi.md#getsectionbyid) | **GET** /api/sections/{sectionId} |  |
| [**getSectionsByEvent**](SectionsApi.md#getsectionsbyevent) | **GET** /api/sections/event/{eventId} |  |



## getSectionById

> SectionResponse getSectionById(sectionId)



### Example

```ts
import {
  Configuration,
  SectionsApi,
} from '';
import type { GetSectionByIdRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SectionsApi();

  const body = {
    // string
    sectionId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GetSectionByIdRequest;

  try {
    const data = await api.getSectionById(body);
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

[**SectionResponse**](SectionResponse.md)

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


## getSectionsByEvent

> Array&lt;SectionResponse&gt; getSectionsByEvent(eventId)



### Example

```ts
import {
  Configuration,
  SectionsApi,
} from '';
import type { GetSectionsByEventRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SectionsApi();

  const body = {
    // string
    eventId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GetSectionsByEventRequest;

  try {
    const data = await api.getSectionsByEvent(body);
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

[**Array&lt;SectionResponse&gt;**](SectionResponse.md)

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

