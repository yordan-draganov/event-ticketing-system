
# SeatResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`sectionId` | string
`sectionName` | string
`sectionPrice` | number
`rowLabel` | string
`seatNumber` | number
`isAvailable` | boolean
`displayLabel` | string

## Example

```typescript
import type { SeatResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "sectionId": null,
  "sectionName": null,
  "sectionPrice": null,
  "rowLabel": null,
  "seatNumber": null,
  "isAvailable": null,
  "displayLabel": null,
} satisfies SeatResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SeatResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


