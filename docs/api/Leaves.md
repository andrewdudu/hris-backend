# Authentication API

## GET Leaves

- Endpoint : `/leaves`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 200,
  "data": {
    "leaves": {
      "annual": 8,
      "extra": ["1231245464", "1237642712"]
    },
    "attendanceAndLeave": {
      "attendance": 230,
      "sick": 5,
      "marriage": 2,
      "childBaptism": 0,
      "childCircumsion": 0,
      "hajj": 0,
      "maternity": 0,
      "childBirth": 0,
      "mainFamDeath": 0,
      "closeFamDeath": 0
    }
  },
  "status": "Success",
  "message": null,
  "path": "/login"
}
```

`data.leaves.extra` is timestamp, and will be removed when expired.

- Response Body (Fail) :

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 401,
  "status": "Unauthorized",
  "message": "Invalid Request",
  "path": "/leaves"
}
```
