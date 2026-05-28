
# TicketResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`userId` | string
`userName` | string
`eventId` | string
`eventTitle` | string
`eventDate` | Date
`eventLocation` | string
`startTime` | string
`endTime` | string
`eventImage` | string
`sectionId` | string
`sectionName` | string
`seatCount` | number
`totalPrice` | number
`status` | string
`purchaseDate` | Date
`qrCodeUrl` | string
`emailSent` | boolean
`emailAttempts` | number
`lastEmailError` | string
`checkedInAt` | Date

## Example

```typescript
import type { TicketResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": null,
  "userId": null,
  "userName": null,
  "eventId": null,
  "eventTitle": null,
  "eventDate": null,
  "eventLocation": null,
  "startTime": 11:00:00,
  "endTime": 12:00:00,
  "eventImage": null,
  "sectionId": null,
  "sectionName": null,
  "seatCount": null,
  "totalPrice": null,
  "status": null,
  "purchaseDate": null,
  "qrCodeUrl": null,
  "emailSent": null,
  "emailAttempts": null,
  "lastEmailError": null,
  "checkedInAt": null,
} satisfies TicketResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as TicketResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


