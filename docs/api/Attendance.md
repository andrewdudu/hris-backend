# Attendances

## Clock in

- Endpoint : `/attendances/_clock-in`
- HTTP Method : `POST`
- `Auth required`
- Image resolution `400 x 800`
- Request Header :
  - Accept : `application/json`
- Request Body :

```json
{
  "image": "base64",
  "location": {
    "lat": 787.123123,
    "lon": 178.123123
  }
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "image": "http://image.webp",
    "location": {
      "lat": 787.123123,
      "lon": 178.123123
    }
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "errors": {
    "location.lat": ["REQUIRED", "INVALID_FORMAT"],
    "image": ["INVALID_FORMAT", "TOO_LARGE"]
  }
}
```

## Clock out

- Endpoint : `/attendances/_clock-out`
- HTTP Method : `POST`
- `Auth required`
- Request Header :
  - Accept : `application/json`
- Request Body :

```json
{
  "location": {
    "lat": 787.123123,
    "lon": 178.123123
  }
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "location": {
      "lat": 787.123123,
      "lon": 178.123123
    }
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "errors": {
    "location.lat": ["REQUIRED", "INVALID_FORMAT"]
  }
}
```

## Attendances

- Endpoint : `/attendances`
- HTTP Method : `GET`
- `Auth required`
- Request Query:
  - username : `string`
  - startDate : `epoch GMT +7`
  - endDate : `epoch GMT +7`
- Request Header :

  - Accept : `application/json`

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    {
      "date": {
        "start": 7817238,
        "end": 1287383
      },
      "location": {
        "type": "INSIDE|OUTSIDE",
        "lat": 787.123123,
        "lon": 178.123123
      }
    },
    {
      "date": {
        "start": 7817238,
        "end": 1287383
      },
      "location": {
        "type": "INSIDE|OUTSIDE",
        "lat": 787.123123,
        "lon": 178.123123
      }
    }
  ]
}
```
