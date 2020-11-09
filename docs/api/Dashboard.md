# Dashboard Summary

## Summary

- Endpoint : `/api/dashboard/summary`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
  - Set-Cookie: `userToken=token`
- Response Body (Success) :

  `reportResponse` and `requestResponse` for admin.

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "reportResponse": {
      "working": 230,
      "absent": 10
    },
    "requestResponse": {
      "incoming": 20
    },
    "calendarResponse": {
      "date": 7817283721,
      "status": "WORKING|HOLIDAY"
    },
    "attendance": {
      "current": {
        "date": {
          "start": 7817238
        },
        "locationResponse": {
          "type": "INSIDE|OUTSIDE"
        }
      },
      "latest": {
        "date": {
          "start": 7817238,
          "end": 1287383
        },
        "locationResponse": {
          "type": "INSIDE|OUTSIDE"
        }
      }
    }
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 401,
  "status": "Unauthorized"
}
```
