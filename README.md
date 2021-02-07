# hris-backend

### Build docker image
    docker build -t hris-be

### Create container
    docker container create --name hris-be -p 8080:8080 hris-be
    
### Start container
    docker container start hris-be