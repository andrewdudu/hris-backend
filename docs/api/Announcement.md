# Announcement API

## GET Announcement

- Endpoint : `/announcements?page=0&size=10`
- HTTP Method : `GET`
- `Auth required`
- Query Params :
    - page `int`
    - size `int`
- Request Header :
  - Accept : `application/json`
- Response Body (Success) :

```json
{
  "code": 200,
  "status": "Success",
  "data": [
    {
      "id": "123767123-12837891273-aldfh123",
      "title": "title",
      "date": 787812738,
      "description": "description"
    }
  ],
  "paging": [
    {
       "page": 0,
       "itemPerPage": 10,
       "totalPage": 20,
       "totalItem": 200
    }
  ] 
}
```
