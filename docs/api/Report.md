# Report API

## GET Reports

- Endpoint : `/api/reports/leaves`
- HTTP Method : `GET`
- Query Param:

  - department `string`
  - month `int`

- Request Header :
  - Accept : `application/json`
  - Set-Cookie: `userToken=token`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    {
      "id": "1273-123-123",
      "employee": {
        "nik": "1273",
        "name": "name",
        "position": {
          "name": "name"
        },
        "department": {
          "name": "Technology"
        },
        "organizationUnit": {
          "name": "name"
        },
        "office": {
          "name": "Sarana Jaya"
        }
      },
      "dateString": "2020-05-25",
      "typeLabel": "Hajj",
      "files": ["url", "url"],
      "date": {
        "start": 7878123,
        "end": 78172312
      },
      "approvedBy": "admin",
      "createdDate": 7878123,
      "createdBy": "username",
      "lastModifiedDate": 787123,
      "lastModifiedBy": "username",
      "notes": ""
    }
  ],
  "paging": {}
}
```
