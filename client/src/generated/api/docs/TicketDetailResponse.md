
# TicketDetailResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`sectionId` | string
`sectionName` | string
`seatCount` | number
`totalPrice` | number
`status` | string
`seats` | [Array&lt;SeatResponse&gt;](SeatResponse.md)
`purchaseDate` | Date
`qrCodeUrl` | string
`emailSent` | boolean
`userId` | string
`userName` | string
`userEmail` | string
`eventId` | string
`eventTitle` | string
`eventDate` | Date
`eventLocation` | string
`eventDescription` | string
`eventLongDescription` | string
`eventCategory` | string
`eventImage` | string
`eventOrganizer` | string
`startTime` | string
`endTime` | string

## Example

```typescript
import type { TicketDetailResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "sectionId": null,
  "sectionName": null,
  "seatCount": null,
  "totalPrice": null,
  "status": null,
  "seats": null,
  "purchaseDate": null,
  "qrCodeUrl": null,
  "emailSent": null,
  "userId": null,
  "userName": null,
  "userEmail": null,
  "eventId": null,
  "eventTitle": null,
  "eventDate": null,
  "eventLocation": null,
  "eventDescription": null,
  "eventLongDescription": null,
  "eventCategory": null,
  "eventImage": null,
  "eventOrganizer": null,
  "startTime": 11:00:00,
  "endTime": 12:00:00,
} satisfies TicketDetailResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as TicketDetailResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


