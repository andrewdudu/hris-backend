# Announcement API

## GET Announcement

- Endpoint : `/announcements`
- HTTP Method : `GET`
- `Auth required`
- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    {
      "id": "123767123-12837891273-aldfh123",
      "title": "title",
      "date": 787812738,
      "description": "description"
    }
  ]
}
```
