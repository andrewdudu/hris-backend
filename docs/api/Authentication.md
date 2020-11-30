# Authentication API

## Employee Login

- Endpoint : `/auth/login`
- HTTP Method : `POST`
- Request Body :

```json
{
  "username": "example@example.com",
  "password": "johndoe123"
}
```

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :
  - Set-Cookie: `userToken=token`

`joinDate` is timestamp.

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "id": "nik user",
    "username": "example@example.com",
    "name": "John Doe",
    "roles": ["EMPLOYEE"],
    "department": "Technology",
    "office": {
      "name": "Sarana Jaya"
    },
    "joinDate": 788781273,
    "leave": {
      "remaining": 10
    }
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "data": null,
  "paging": null,
  "errors": {
    "credential": ["DOES_NOT_MATCH"]
  }
}
```
