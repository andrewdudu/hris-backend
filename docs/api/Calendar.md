# Calendar API

## GET Calendar

- Endpoint : `/api/calendar/days`
- HTTP Method : `GET`
- Query Param:
  - month `int`
  - year `int`
- `Auth required`
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
      "date": 7871823,
      "status": "WORKING|HOLIDAY",
      "events": [
        {
          "name": "Independence Day"
        }
      ]
    }
  ]
}
```

## Set Holiday

`date` format ex: `2020-02-25`

- Endpoint : `/calendar/days/{date}/events`
- HTTP Method : `POST`
- `Auth required`
- Request Header :
  - Accept : `application/json`
- Request Body :

```json
{
  "name": "name",
  "notes": "notes",
  "status": "HOLIDAY"
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "name": "Independence Day",
    "notes": "notes",
    "status": "HOLIDAY"
  }
}
```
