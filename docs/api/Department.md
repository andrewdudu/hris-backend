# Department API

## GET Department

- Endpoint : `/api/deparments`
- HTTP Method : `GET`
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
      "name": "Human Resources",
      "code": "HUMAN_RESOURCES"
    },
    {
      "name": "Marketing",
      "code": "MARKETING"
    },
    {
      "name": "Operation",
      "code": "OPERATION"
    },
    {
      "name": "Technology",
      "code": "TECHNOLOGY"
    }
  ]
}
```
