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
      "annual": {
        "remaining": 10,
        "used": 2,
        "expire": "783716263"
      },
      "extra": {
        "remaining": 3,
        "used": 1,
        "expire": "787182333"
      },
      "substitute": ["787812378", "6787812378"]
    }
  },
  "status": "Success",
  "message": null,
  "path": "/login"
}
```

`data.leaves.substitute`, `data.leaves.extra.expire` and `data.leaves.annual.expire` is timestamp, and will be removed when expired.

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
