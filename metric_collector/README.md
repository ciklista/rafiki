# Metric collector

## Deploy

Build your the jar of the app.

```
JAR_FILE=[PATH/TO/.jar] docker-compose up -d
```

The database will be reachable from port `5432` from the docker network and from port `30050` on the host network.

## Development
The postgres will be reachable from the host network from port `30050`.
In order to only start the postgres container run:
```
docker-compose up postgres
```