# Metric collector

## Deploy

Build your the jar of the app.

To run the docker containers: 
```
JAR_FILE=[PATH/TO/.jar] docker-compose up -d
```

The database will be reachable from `postgres:5432` on the docker network and from `localhost:30050` on the host network.

## Development
The postgres will be reachable from the host network from `localhost:30050`.
In order to only start the postgres container run:
```
docker-compose up postgres
```
