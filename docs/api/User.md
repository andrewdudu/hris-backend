# User API

## GET User

- Endpoint : `/api/users/current-user`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
  - Set-Cookie: `userToken=token`
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

## GET Leaves Quotas

- Endpoint : `/users/{id}/leave-quotas`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    {
      "type": "annual",
      "remaining": 10,
      "used": 2,
      "expiry": 783716263
    },
    {
      "type": "extra",
      "remaining": 3,
      "used": 1,
      "expiry": 787182333
    },
    {
      "type": "substitute",
      "remaining": 2,
      "used": 1,
      "expiries": [787812378, 6787812378]
    }
  ]
}
```

## GET Leaves Quotas

- Endpoint : `/users/{id}/profile`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "attendance": 230,
    "leave": {
      "sick": 5,
      "marriage": 2,
      "childBaptism": 2,
      "childCircumcision": 0,
      "childBirth": 0,
      "hajj": 0,
      "maternity": 0,
      "mainFamilyDeath": 0,
      "closeFamilyDeath": 0
    },
    "quota": {
      "annual": 8,
      "extra": 2
    }
  }
}
```

## Available Requests

- Endpoint : `/users/current-user/available-requests`
- HTTP Method : `GET`
- `Auth required`
- Request Header :

  - Accept : `application/json`

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    "ATTENDANCE",
    "ANNUAL_LEAVE",
    "SPECIAL_LEAVE",
    "EXTRA_LEAVE",
    "SUBTITUTE_LEAVE",
    "EXTEND_ANNUAL_LEAVE"
  ]
}
```

## Available Special Requests

- Endpoint : `/users/current-user/available-special-requests`
- HTTP Method : `GET`
- `Auth required`
- Request Header :

  - Accept : `application/json`

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    "SICK",
    "SICK_WITH_MEDICAL_LETTER",
    "MARRIAGE",
    "MATERNITY",
    "CHILDBIRTH",
    "MAIN_FAMILY_DEATH",
    "CLOSE_FAMILY_DEATH",
    "HAJJ",
    "CHILD_BAPTISM",
    "CHILD_CIRCUMSION",
    "UNPAID_LEAVE"
  ]
}
```

## GET Attendnace Summary

- Endpoint : `/users/current-user/attendance-summary`
- HTTP Method : `GET`

- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "month": {
      "attendance": 120,
      "absent": 2
    },
    "year": {
      "attendance": 120,
      "absent": 2
    }
  }
}
```
