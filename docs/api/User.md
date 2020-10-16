# User API

## GET User

- Endpoint : `/users/current-user`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

`joinDate` is timestamp.

```json
{
  "code": 200,
  "status": "Success",
  "data": {
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
  "code": 401,
  "status": "Unauthorized"
}
```
