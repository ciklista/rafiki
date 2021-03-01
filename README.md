# Rafiki: Operator level capacity planning for DSPs
Rafiki is a tool that collects information on the capacity of single DSP operators according to their individual parallelism settings. Rafiki includes a UI that allows to start the information collection (experiments) and can display live capactiy information on a running job.
We currently support flink jobs, but Rafiki can easily be expanded to support other DSPs as well. 

## Deploy
Move to the rafiki directory:
```
cd rafiki
```

Build the backend java springboot app:
```
cd app && mvn package
```

Start the docker containers:
```
docker-compose up -d
```

The UI is reachable from [http://localhost:3000/](http://localhost:3000/).

## Test & develop
We provide a test setup including a flink cluster running on Kubernetes and two example flink jobs: the [yahoo benchmark](example_jobs/yahoo-benchmark/) and an [IoT inspired job](example_jobs/iot-vehicles-experiment/).

### Infrastructrue
#### Create a Kubernetes cluster on Google Cloud

The following commands assume that a project has been created in GCP and ``gcloud`` has been configured for use.

Set project:
```
gcloud config set project mpds-297011
```

Set compute zone:
```
gcloud config set compute/zone europe-west3-c
```

Deploy a 3 node Kubernetes cluster with the e2-standard-4 machine-type with:
```
gcloud container clusters create mpds-cluster --num-nodes=3  \
  --image-type "UBUNTU" --no-enable-basic-auth  --no-enable-autoupgrade \
  --machine-type e2-standard-4
```

After the Kubernetes cluster has been created, configure kubectl with:
```
gcloud container clusters get-credentials mpds-cluster
```

To access the Kubernetes cluster from a browser, open up the firewall with:
```
gcloud compute firewall-rules create nodeports \
  --allow tcp --source-ranges=0.0.0.0/0
```

#### Deploy Helm charts
Kafka, Flink, Redis, Prometheus, and Grafana can be deployed on a Kubernetes cluster using the [Helm](https://helm.sh) charts located in the ``helm-charts`` directory. Configure which charts to deploy in the global values.yaml by setting ``enabled: true`` for each desired technology. Cluster sizes and ports for external access can also be specified here.

Each subchart can be deployed by itself and contains its own values.yaml file with futher configurations. If deployed from the umbrella chart, values in the global values.yaml will overwrite the values in the subchart's values.yaml.

To deplay all helm charts at once:
```
cd helm-charts && helm install mpds .
```

To deploy single charts:
```
helm install [DEPLOYMENT NAME] [CHART DIRECTORY]
```

Uninstall the charts with:
```
helm uninstall [DEPLOYMENT NAME]
```

#### Viewing metrics in Grafana

Grafana is accessible at <kubernetes_node_ip>:<nodeport>.
The default nodeport is ``30080`` and the default username and password is ``admin``

After logging into Grafana, the data source must be added.
Navigate to: ``Configuration > Data Sources > Add data source > Prometheus``
Set the Url to ``prometheus:9090`` and click save and test. You should see a green notification that the data source is working.

To import the premade grafana dashboard to show metrics, navigate to:
``Create > Import > Upload JSON file``
Upload the ``grafana-dashboard.json`` file from the root directory.
