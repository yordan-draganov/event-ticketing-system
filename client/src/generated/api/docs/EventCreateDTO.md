
# EventCreateDTO


## Properties

Name | Type
------------ | -------------
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
`sections` | [Array&lt;SectionRequestDTO&gt;](SectionRequestDTO.md)

## Example

```typescript
import type { EventCreateDTO } from ''

// TODO: Update the object below with actual values
const example = {
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
  "sections": null,
} satisfies EventCreateDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EventCreateDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


