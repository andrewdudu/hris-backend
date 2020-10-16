# Profile API

## GET Profile

- Endpoint : `/profile`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "role": "employee",
    "name": "John Doe",
    "email": "example@example.com",
    "team": "Technology",
    "office": "Sarana Jaya",
    "join": "12342134123",
    "leaves": {
      "annual": 8,
      "extra": 2
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
  "paging": null,
  "errors": null
}
```

`data.join` is timestamp.

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
