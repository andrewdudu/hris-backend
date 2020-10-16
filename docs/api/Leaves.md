# Authentication API

## GET Leaves

- Endpoint : `/leaves`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
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
  "paging": null,
  "errors": null
}
```

`data.leaves.substitute`, `data.leaves.extra.expire` and `data.leaves.annual.expire` is timestamp, and will be removed when expired.

- Response Body (Fail) :

```json
{
  "code": 401,
  "status": "Unauthorized",
  "data": null,
  "paging": null,
  "errors": {
    "message": "You are not logged in"
  }
}
```
