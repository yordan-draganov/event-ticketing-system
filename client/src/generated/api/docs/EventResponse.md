
# EventResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`title` | string
`date` | Date
`location` | string
`description` | string
`longDescription` | string
`category` | string
`image` | string
`organizer` | string
`startTime` | string
`endTime` | string
`isFinished` | boolean
`minPrice` | number
`maxPrice` | number
`totalSeats` | number
`availableSeats` | number
`sectionCount` | number

## Example

```typescript
import type { EventResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "title": null,
  "date": null,
  "location": null,
  "description": null,
  "longDescription": null,
  "category": null,
  "image": null,
  "organizer": null,
  "startTime": 11:00:00,
  "endTime": 12:00:00,
  "isFinished": null,
  "minPrice": null,
  "maxPrice": null,
  "totalSeats": null,
  "availableSeats": null,
  "sectionCount": null,
} satisfies EventResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EventResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


