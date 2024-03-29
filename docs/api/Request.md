# Request

## Request Attendance

- Endpoint : `/api/request/attendances`
- HTTP Method : `POST`
- Request Header :
  - Accept : `application/json`
  - Set-Cookie: `userToken=token`
- Request Body :

```json
{
  "date": "2020-05-25",
  "clockIn": "08:00",
  "clockOut": "17:00",
  "notes": "notes"
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "date": "2020-05-25",
    "clockIn": "08:00",
    "clockOut": "17:00",
    "notes": "notes"
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "errors": {
    "date": ["INVALID_FORMAT"],
    "clockIn": ["INVALID_FORMAT"]
  }
}
```

## Request Leaves

- Endpoint : `/request/leaves`
- HTTP Method : `POST`

- Request Header :
  - Accept : `application/json`
- Request Body :

```json
{
  "dates": ["2020-09-25"],
  "files": ["base64"],
  "notes": "notes",
  "type": "SICK"
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "dates": ["2020-09-25"],
    "files": ["http://file.pdf", "http://file.webp"],
    "notes": "notes",
    "type": "SICK"
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "errors": {
    "dates": ["INVALID_FORMAT"],
    "files": ["INVALID_FORMAT"],
    "type": ["QUOTA_NOT_AVAILABLE"]
  }
}
```

## GET Extend Leaves data

- Endpoint : `/request/extend-leave`
- HTTP Method : `GET`

- Request Header :

  - Accept : `application/json`

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "status": "REQUESTED|UNAVAILABLE|AVAILABLE",
    "quota": {
      "remaining": 8,
      "extensionDate": 78127831
    }
  }
}
```

## Request Extend Leave

- Endpoint : `/request/extend-leave`
- HTTP Method : `POST`

- Request Header :

  - Accept : `application/json`

- Request Body :

```json
{
  "notes": "notes"
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "status": "REQUESTED|UNAVAILABLE",
    "notes": "notes"
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "errors": {
    "status": ["REQUESTED"]
  }
}
```

## Incoming Request

- Endpoint : `/requests`
- HTTP Method : `GET`
- Query Param:
  - type: `REQUESTED|APPROVED|REJECTED`
  - department: `dep-code`
  - page: `int`
  - size: `int`
- Request Header :

  - Accept : `application/json`

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    {
      "id": "1823a87f-12387321adf-123123adf",
      "user": {
        "name": "John Doe",
        "department": "Technology",
        "office": {
          "name": "Sarana Jaya"
        }
      },
      "status": "REQUESTED",
      "type": "LEAVE|EXTEND|ATTENDANCE",
      "detail": {
        "attendance": {
          "date": {
            "start": 78123,
            "end": 78123
          },
          "notes": "notes"
        }
      },
      "date": 7871823
    },
    {
      "id": "1823a87f-12387321adf-123123adf",
      "user": {
        "name": "John Doe",
        "department": "Technology",
        "office": {
          "name": "Sarana Jaya"
        }
      },
      "status": "REQUESTED",
      "type": "LEAVE|EXTEND|ATTENDANCE",
      "detail": {
        "leave": {
          "dates": ["2020-09-25"],
          "files": ["http://file.pdf", "http://file.webp"],
          "notes": "notes",
          "type": "SICK"
        }
      },
      "date": 7871823
    },
    {
      "id": "1823a87f-12387321adf-123123adf",
      "user": {
        "name": "John Doe",
        "department": "Technology",
        "office": {
          "name": "Sarana Jaya"
        }
      },
      "status": "REQUESTED",
      "type": "LEAVE|EXTEND|ATTENDANCE",
      "detail": {
        "extend": {
          "notes": "notes"
        }
      },
      "date": 7871823
    }
  ]
}
```

- Response Body (Fail) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "errors": {
    "status": ["REQUESTED"]
  }
}
```

## Bulk Approve

- Endpoint : `/requests/_approve`
- HTTP Method : `POST`
- Request Header :

  - Accept : `application/json`
  
- Request Body : 

```json
{
  "ids": [
    "1823a87f-12387321adf-123123adf",
    "1823a87f-12387321adf-123123adff",
    "1823a87f-12387321adf-123123adfa"
  ]
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "ids": [
       "1823a87f-12387321adf-123123adf",
       "1823a87f-12387321adf-123123adff",
       "1823a87f-12387321adf-123123adfa"
     ]
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 403,
  "status": "Forbidden"
}
```

## Approve

- Endpoint : `/requests/{id}/_approve`
- HTTP Method : `POST`
- Request Header :

  - Accept : `application/json`

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "id": "1823a87f-12387321adf-123123adf",
    "user": {
      "name": "John Doe",
      "department": "Technology",
      "office": {
        "name": "Sarana Jaya"
      }
    },
    "status": "APPROVED",
    "type": "LEAVE|EXTEND|ATTENDANCE",
    "detail": {
      "attendance": {
        "date": {
          "start": 78123,
          "end": 78123
        },
        "notes": "notes"
      }
    },
    "date": 7871823
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 403,
  "status": "Forbidden"
}
```

## Reject

- Endpoint : `/requests/{id}/_reject`
- HTTP Method : `POST`
- Request Header :

  - Accept : `application/json`

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "id": "1823a87f-12387321adf-123123adf",
    "user": {
      "name": "John Doe",
      "department": "Technology",
      "office": {
        "name": "Sarana Jaya"
      }
    },
    "status": "REJECTED",
    "type": "LEAVE|EXTEND|ATTENDANCE",
    "detail": {
      "attendance": {
        "date": {
          "start": 78123,
          "end": 78123
        },
        "notes": "notes"
      }
    },
    "date": 7871823
  }
}
```

- Response Body (Fail) :

```json
{
  "code": 403,
  "status": "Forbidden"
}
```

## Manual Add Substitute Leave (Admin)

- Endpoint : `/substitute-leave`
- HTTP Method : `POST`
- Request Header :

  - Accept : `application/json`

- Request Body :
```json
{ 
  "id": "id",
  "total": 3
}
```
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": {
    "id": "1823a87f-12387321adf-123123adf",
    "total": 3
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

## Request Hourly Leave

- Endpoint : `/request/hourly`
- HTTP Method : `POST`
- Request Header :

  - Accept : `application/json`

- Request Body :
```json
{ 
  "startTime": "11:00",
  "endTime": "13:00",
  "notes": "notes"
}
```
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": { 
    "startTime": "11:00",
    "endTime": "13:00",
    "notes": "notes"
  }
}
```
