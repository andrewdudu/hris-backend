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
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 200,
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
  "status": "Success",
  "message": null,
  "path": "/auth/login"
}
```

`data.user.join` is timestamp.

- Response Body (Fail) :

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 400,
  "status": "Bad Request",
  "message": "Invalid Request: Invalid user authentication or invalid request format",
  "path": "/auth/login"
}
```

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 401,
  "status": "Unauthorized",
  "message": "Invalid Request: username/email or password is wrong",
  "path": "/auth/login"
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
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 200,
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
  "status": "Success",
  "message": null,
  "path": "/auth/login/manager"
}
```

`data.user.join` is timestamp.

- Response Body (Fail) :

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 400,
  "status": "Bad Request",
  "message": "Invalid Request: Invalid user authentication or invalid request format",
  "path": "/auth/login/manager"
}
```

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 401,
  "status": "Unauthorized",
  "message": "Invalid Request: username/email or password is wrong",
  "path": "/auth/login/manager"
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
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 200,
  "data": {
    "user": {
      "role": "admin",
      "name": "John Doe",
      "email": "example@example.com",
      "requests": 20
    }
  },
  "status": "Success",
  "message": null,
  "path": "/auth/login/admin"
}
```

`data.user.join` is timestamp.

- Response Body (Fail) :

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 400,
  "status": "Bad Request",
  "message": "Invalid Request: Invalid user authentication or invalid request format",
  "path": "/auth/login/admin"
}
```

```json
{
  "timestamp": "2019-08-23T04:22:26.690+0000",
  "code": 401,
  "status": "Unauthorized",
  "message": "Invalid Request: username/email or password is wrong",
  "path": "/auth/login/admin"
}
```
