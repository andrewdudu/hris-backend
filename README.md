# HRIS-BACKEND

### Build docker image
    docker build -t hris-be

### Create container
    docker container create --name hris-be -p 8000:8000 hris-be
    
### Start container
    docker container start hris-be
    
    
# RUNDECK

### Auto-Clockout
    curl -c /cookie -H "Content-Type: application/json" --request POST \   -d '{\"username\":\"admin@mail.com\", \"password\":\"adminpass\"}' http://localhost:8081/auth/login
    curl -b /cookie --request POST 'http://localhost:8081/api/scheduler/auto-clockout'
    
### Auto Update Leave Quota
    curl -c /cookie -H "Content-Type: application/json" --request POST \   -d '{\"username\":\"admin@mail.com\", \"password\":\"adminpass\"}' http://localhost:8081/auth/login
    curl -b /cookie --request POST 'http://localhost:8081/api/scheduler/update-leave'