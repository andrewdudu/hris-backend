# Dashboard Summary

## Summary

- Endpoint : `/dashboard/summary`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

  `report` and `request` for admin.

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "report": {
      "working": 230,
      "absent": 10
    },
    "request": {
      "incoming": 20
    },
    "calendar": {
      "date": 7817283721,
      "status": "WORKING|HOLIDAY"
    },
    "attendance": {
      "current": {
        "date": {
          "start": 7817238
        },
        "location": {
          "type": "INSIDE|OUTSIDE"
        }
      },
      "latest": {
        "date": {
          "start": 7817238,
          "end": 1287383
        },
        "location": {
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
