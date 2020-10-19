# Department API

## GET Department

- Endpoint : `/deparments`
- HTTP Method : `GET`
- Request Header :
  - Accept : `application/json`
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
