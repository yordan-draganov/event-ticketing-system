# UsersApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**changeName**](UsersApi.md#changenameoperation) | **PATCH** /api/users/name | Change name |
| [**changePassword**](UsersApi.md#changepasswordoperation) | **PATCH** /api/users/pass | Change password |
| [**deleteUser**](UsersApi.md#deleteuser) | **DELETE** /api/users/delete | Delete user account |
| [**getAllUsers**](UsersApi.md#getallusers) | **GET** /api/users/all | Get all users |
| [**getCurrentUser**](UsersApi.md#getcurrentuser) | **GET** /api/users/me | Get current user |
| [**getUserById**](UsersApi.md#getuserbyid) | **GET** /api/users/{id} | Get user by ID |
| [**getUserRole**](UsersApi.md#getuserrole) | **GET** /api/users/role/{name} | Get user role |
| [**login**](UsersApi.md#loginoperation) | **POST** /api/users/login | Login user |
| [**logout**](UsersApi.md#logout) | **POST** /api/users/logout | Logout user |
| [**refresh**](UsersApi.md#refresh) | **POST** /api/users/refresh | Refresh access token |
| [**signUp**](UsersApi.md#signup) | **POST** /api/users/signup | Register new user |



## changeName

> AuthResponse changeName(changeNameRequest)

Change name

Update user display name

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { ChangeNameOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  const body = {
    // ChangeNameRequest
    changeNameRequest: ...,
  } satisfies ChangeNameOperationRequest;

  try {
    const data = await api.changeName(body);
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
| **changeNameRequest** | [ChangeNameRequest](ChangeNameRequest.md) |  | |

### Return type

[**AuthResponse**](AuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## changePassword

> string changePassword(changePasswordRequest)

Change password

Update user password

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { ChangePasswordOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  const body = {
    // ChangePasswordRequest
    changePasswordRequest: ...,
  } satisfies ChangePasswordOperationRequest;

  try {
    const data = await api.changePassword(body);
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
| **changePasswordRequest** | [ChangePasswordRequest](ChangePasswordRequest.md) |  | |

### Return type

**string**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## deleteUser

> string deleteUser()

Delete user account

Delete current user\&#39;s account

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { DeleteUserRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  try {
    const data = await api.deleteUser();
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

**string**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getAllUsers

> Array&lt;UserDTO&gt; getAllUsers()

Get all users

Retrieve all users (Admin only)

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { GetAllUsersRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  try {
    const data = await api.getAllUsers();
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

[**Array&lt;UserDTO&gt;**](UserDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getCurrentUser

> UserDTO getCurrentUser()

Get current user

Get authenticated user\&#39;s profile

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { GetCurrentUserRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  try {
    const data = await api.getCurrentUser();
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

[**UserDTO**](UserDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getUserById

> UserDTO getUserById(id)

Get user by ID

Retrieve user information by UUID

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { GetUserByIdRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies GetUserByIdRequest;

  try {
    const data = await api.getUserById(body);
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

[**UserDTO**](UserDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getUserRole

> string getUserRole(name)

Get user role

Retrieve user role by username

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { GetUserRoleRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  const body = {
    // string
    name: name_example,
  } satisfies GetUserRoleRequest;

  try {
    const data = await api.getUserRole(body);
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
| **name** | `string` |  | [Defaults to `undefined`] |

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
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## login

> AuthResponse login(loginRequest)

Login user

Authenticate user and return JWT token

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { LoginOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  const body = {
    // LoginRequest
    loginRequest: ...,
  } satisfies LoginOperationRequest;

  try {
    const data = await api.login(body);
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
| **loginRequest** | [LoginRequest](LoginRequest.md) |  | |

### Return type

[**AuthResponse**](AuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Invalid credentials |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | Login successful |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## logout

> string logout()

Logout user

Invalidate current JWT token

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { LogoutRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  try {
    const data = await api.logout();
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

**string**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## refresh

> AuthResponse refresh()

Refresh access token

Issue a new short-lived access token from the HttpOnly refresh token cookie

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { RefreshRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  try {
    const data = await api.refresh();
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

[**AuthResponse**](AuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Bad Request |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## signUp

> AuthResponse signUp(signupRequest)

Register new user

Create a new user account

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { SignUpRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new UsersApi();

  const body = {
    // SignupRequest
    signupRequest: ...,
  } satisfies SignUpRequest;

  try {
    const data = await api.signUp(body);
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
| **signupRequest** | [SignupRequest](SignupRequest.md) |  | |

### Return type

[**AuthResponse**](AuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **400** | Invalid input or user already exists |  -  |
| **409** | Conflict |  -  |
| **503** | Service Unavailable |  -  |
| **500** | Internal Server Error |  -  |
| **402** | Payment Required |  -  |
| **200** | User registered successfully |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

