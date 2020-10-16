# Request

## Request Attendance

- Endpoint : `/request/attendances`
- HTTP Method : `POST`
- Request Header :
  - Accept : `application/json`
- Request Body :

```json
{
  "date": "2020-05-25",
  "clockIn": "08:00",
  "clockOut": "17:00",
  "notes": "notes"
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "date": "2020-05-25",
    "clockIn": "08:00",
    "clockOut": "17:00",
    "notes": "notes"
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "errors": {
    "date": ["INVALID_FORMAT"],
    "clockIn": ["INVALID_FORMAT"]
  }
}
```
