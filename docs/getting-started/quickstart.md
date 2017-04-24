---
layout: doc
weight: 1
title: "Loom Quickstart"
categories: getting-started
---

### Prerequisites

- Access to an active pair of AWS credentials.
- [Docker](https://docker.io).
- A Route 53 controlled domain (e.g. `example.org`).

### 1. Run Loom

**NOTE**: Loom is Alpha quality software. It is strongly recommended you do a `docker pull datawire/loom:alpha` frequently to ensure the latest image is running.

Loom is packaged as a [Docker](https://docker.com) image. It can be started with a `docker run ...` command shown below. When Loom runs for the first time it will create some necessary core infrastructure on your AWS account during a bootstrap phase:

- An AWS S3 bucket where Loom can store app state and config. The name will be `loom-state-${AWS_ACCOUNT_ID}`.
- An AWS DynamoDB table named `loom_terraform_state_lock` which is used internally when provisioning for state management safety against concurrent executions.

Depending on how you store your AWS credentials and config there are two common run options:

**Preferred: Use AWS credentials and config in `$HOME/.aws` directory**

```bash
docker pull datawire/loom:alpha
docker run -p 7000:7000 -v ${HOME}/.aws:/root/.aws --rm -it datawire/loom:alpha
```

**Alternative: Set AWS environment variables**

```bash
docker pull datawire/loom:alpha
docker run --rm -it \
  -p 7000:7000 \
  -e AWS_ACCESS_KEY_ID=<Your-AWS-API-Access-Key> \
  -e AWS_SECRET_ACCESS_KEY=<Your-AWS-API-Secret-Key> \
  -e AWS_REGION=us-east-1 \
  datawire/loom:alpha
```

When Loom starts you will see some output similar to below

```text
[...]

2017-03-29 06:17:14.354 INFO [main] i.d.l.c.Bootstrap - AWS bootstrap started
2017-03-29 06:17:14.553 INFO [main] i.d.l.c.Bootstrap - AWS S3 bucket for Loom state store created: loom-state-XXX
2017-03-29 06:17:14.761 INFO [main] i.d.l.c.Bootstrap - AWS DynamoDB table for Loom terraform state locks created: loom_terraform_state_lock
2017-03-29 06:17:14.761 INFO [main] i.d.l.c.Bootstrap - AWS bootstrap completed
2017-03-29 06:17:14.773 INFO [main] i.d.l.Loom - == Loom has started ...
2017-03-29 06:17:14.773 INFO [main] i.d.l.Loom - >> Listening on 0.0.0.0:7000
```

Once you see the Loom `>> Listening on 0.0.0.0:7000` message you can start playing with Loom.

### 2. Define a Fabric Model

Open a second terminal that can act as your client to interact with the Loom server.

Loom has a very important concept of a 'Fabric Model' which basically a reusable configuration that Fabrics created by Loom can use to simplify configuration. The purpose of a "Fabric Model" is to keep the Operations Engineer in control of most operational parameters for a cluster such as count or size of Kubernetes nodes, the SSH public key to assign to instances or the root domain to use for all clusters.

Consider a scenario as an ops engineer where you want to allow developers to spin up very small `t2.nano` powered Kubernetes clusters for experimentation or CI tests without handing over full control or exposing unnecessary complexity. Let's create our first model named `simple` and uses the domain name `example.org` (substitute your own domain for this exercise).

1. Create an SSH key pair that can be attached to the underlying Kubernetes master and worker nodes:

   `ssh-keygen -f ~/.ssh/loom.key -t rsa -b 4096 -N ''`

2. Create a new Fabric Model:

   ```bash
   curl -v -X POST \
        -H "Content-Type: application/json" \
        -d '{"name": "simple", "version": 1, "domain": "${YOUR_DOMAIN}", "sshPublicKey": "'"$(cat ~/.ssh/loom.key.pub)"'"}' \
        localhost:7000/models
   ```

Once a model is registered many clusters can reuse it! Models are identified as `${NAME}-v${VERSION}` so the above model would be `simple-v1`.

### 3. Startup a Kubernetes Fabric

Let's start a fabric! Choose a name for your fabric, for example: `philsfab` or `testing`. We'll use the Model we setup in the previous step which

```bash
curl -v -X POST \
     -H "Content-Type: application/json" \
     -d '{"name": "myfirstfabric", "model": "simple-v1"}' \
     localhost:7000/fabrics
```

### 4. Get your Kubernetes credentials

Talking to Kubernetes still requires credentials and you need Loom to give them to you:

```bash
mkdir ~/.kube/config.d
curl --output ~/.kube/config.d/myfirstcluster.cluster \
    localhost:7000/fabrics/myfirstcluster/cluster/config
```

### 5. Check if Cluster is Available

Getting the status of a cluster is easy:

```bash
curl localhost:7000/fabrics/myfirstfabric/cluster

{
  "name": "myfirstcluster.example.org",
  "available": true
}
```

### 5. Talking to Kubernetes

Once the cluster is available you can use `kubectl` to use Kubernetes:

```bash
kubectl cluster-info --kubeconfig=${HOME}/.kube/config.d/myfirstfabric.cluster
Kubernetes master is running at <Some-URL>
KubeDNS is running at <Some-URL>
```

Cluster up and running! Now you can use `kubectl` to actually do work with Kubernetes.

**NOTE:** The `kubectl` command does not *yet* understand the `config.d` idiom, but there is a Pull Request moving along to enable this functionality in `kubectl`. The idea is that `kubectl` would load all the config files in this directory before use. Until then we need to simulate usage with the `--kubeconfig=<path>` option.
