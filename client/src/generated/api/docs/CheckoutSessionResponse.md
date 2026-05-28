
# CheckoutSessionResponse


## Properties

Name | Type
------------ | -------------
`event` | [EventResponse](EventResponse.md)
`sections` | [Array&lt;SectionResponse&gt;](SectionResponse.md)
`selectedSeats` | [Array&lt;SeatResponse&gt;](SeatResponse.md)
`selectedSection` | [SectionResponse](SectionResponse.md)
`totalPrice` | number
`payment` | [PaymentResponse](PaymentResponse.md)

## Example

```typescript
import type { CheckoutSessionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "event": null,
  "sections": null,
  "selectedSeats": null,
  "selectedSection": null,
  "totalPrice": null,
  "payment": null,
} satisfies CheckoutSessionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CheckoutSessionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


