# Authentication API

## Employee Login

- Endpoint : `/auth/login`
- HTTP Method : `POST`
- Request Body :

```json
{
  "email": "example@example.com",
  "password": "johndoe123"
}
```

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :
  - Set-Cookie: `accessToken=token,refreshToken=token`

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "user": {
      "role": "employee",
      "name": "John Doe",
      "email": "example@example.com",
      "team": "Technology",
      "office": "Sarana Jaya",
      "join": "12342134123"
    }
  },
  "paging": null,
  "errors": null
}
```

`data.user.join` is timestamp.

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "data": null,
  "paging": null,
  "errors": {
    "email": "Email does not exist",
    "password": "Password mismatch"
  }
}
```

## Manager Login

- Endpoint : `/auth/login/manager`
- HTTP Method : `POST`
- Request Body :

```json
{
  "email": "example@example.com",
  "password": "johndoe123"
}
```

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :
  - Set-Cookie: `accessToken=token,refreshToken=token`

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "user": {
      "role": "manager",
      "name": "John Doe",
      "email": "example@example.com",
      "team": "Technology",
      "office": "Sarana Jaya",
      "join": "12342134123"
    }
  },
  "paging": null,
  "errors": null
}
```

`data.user.join` is timestamp.

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "data": null,
  "paging": null,
  "errors": null
}
```

## Admin Login

- Endpoint : `/auth/login/admin`
- HTTP Method : `POST`
- Request Body :

```json
{
  "email": "example@example.com",
  "password": "johndoe123"
}
```

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :
  - Set-Cookie: `accessToken=token,refreshToken=token`

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "user": {
      "role": "admin",
      "name": "John Doe",
      "email": "example@example.com",
      "requests": 20
    }
  },
  "paging": null,
  "errors": null
}
```

`data.user.join` is timestamp.

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "data": null,
  "paging": null,
  "errors": null
}
```
