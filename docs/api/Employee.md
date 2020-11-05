# Employee API

## GET Employee

- Endpoint : `/api/employees`
- HTTP Method : `GET`
- Query Param:

  - department `string`
  - name `string`

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
      "name": "name",
      "department": "Technology",
      "office": {
        "name": "Sarana Jaya"
      }
    }
  ],
  "paging": {}
}
```

## GET Employee Detail

- Endpoint : `/employee/{id}`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "user": {
      "name": "name",
      "department": "Technology",
      "office": {
        "name": "Sarana Jaya"
      }
    },
    "attendance": {
      "image": "http://image.webp",
      "date": {
        "start": 7817238,
        "end": 1287383
      },
      "locationResponse": {
        "type": "INSIDE|OUTSIDE",
        "lat": 787.123123,
        "lon": 178.123123
      }
    }
  }
}
```
