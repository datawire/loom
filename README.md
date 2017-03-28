# Loom

[![Build Status](https://travis-ci.org/datawire/loom.svg?branch=master)](https://travis-ci.org/datawire/loom)

Loom enables operations engineers to provide a self-serve Kubernetes provisioning experience for developers and much more! Developers love Kubernetes, but it's a pain to get up and running on AWS and ops engineers usually have better things to be doing than babysitting devs as they get up and running with Kubernetes.

Thus we have Loom! Operators install Loom inside of their AWS account as a persistent running server and developers use the simple HTTP API to self provision their own Kubernetes fabrics. Loom handles all the nitty gritty details of network creation and cluster management.

## What is a Kubernetes "Fabric"?

When people talk about Kubernetes they usually talk purely in terms of the Kubernetes cluster where containers are scheduled and run. A "fabric" is an abstract concept that describes the entire ecosystem surrounding a Kubernetes cluster, for example, a "Kubernetes" and "AWS" fabric includes not only the Kubernetes cluster, but one or more VPC and within that VPC you may schedule non-Kubernetes resources to run such as RDS databases or EC2 instances. The point of calling it a "fabric" is that it's all nicely woven together for you so that, for example, containers running in the Kubernetes cluster can speak to the RDS databases without having to think about networking.

## Getting Started in Five Minutes

This is a simple demonstration of Loom. For more detailed install instructions follow the [Detailed Install Guide](install/README.md).

### Prerequisites

- Access to an active pair of AWS credentials.
- [Docker](https://docker.io)

### 1. Run Loom

Loom is packaged as a Docker image. It can be started with a `docker run ...` command shown below. When Loom runs for the first time it will create some necessary core infrastructure on your AWS account during a bootstrap phase:

- An AWS S3 bucket where Loom can store app state and config. The name will be `loom-state-${AWS_ACCOUNT_ID}`.
- An AWS DynamoDB table named `loom_terraform_state_lock` which is used internally when provisioning for state management safety against concurrent executions.

```
$> docker run --rm -it \
    -p 7000:7000 \
    -e AWS_ACCESS_KEY_ID=<Your-AWS-API-Access-Key> \
    -e AWS_SECRET_ACCESS_KEY=<Your-AWS-API-Secret-Key> \
    -e AWS_REGION=us-east-1 \
    datawire/loom:0.1.0

2017-03-23 19:39:29.144 INFO [vert.x-eventloop-thread-0] i.d.l.v.Loom - Loom starting...
2017-03-23 19:39:30.007 INFO [vert.x-eventloop-thread-0] i.d.l.v.s.Bootstrap - AWS bootstrap started
2017-03-23 19:39:30.235 INFO [vert.x-eventloop-thread-0] i.d.l.v.s.Bootstrap - AWS S3 bucket for Loom state store created: loom-state-914373874199
2017-03-23 19:39:30.428 INFO [vert.x-eventloop-thread-0] i.d.l.v.s.Bootstrap - AWS DynamoDB table for Loom terraform state locks created: loom_terraform_state_lock
2017-03-23 19:39:30.428 INFO [vert.x-eventloop-thread-0] i.d.l.v.s.Bootstrap - AWS bootstrap completed
2017-03-23 19:39:30.610 INFO [vert.x-eventloop-thread-0] i.d.l.v.Loom - Loom started! Listening @ http://0.0.0.0:7000
```

Once you see the Loom `Loom started! Listening @ http://0.0.0.0:7000` message you can start playing with Loom.

### 2. Define a Fabric Model

Open a second terminal that can act as your client to interact with the running Loom server.

Loom has a very important concept of a 'Fabric Model' which basically a reusable template and configuration that many fabrics deployed by loom can use to simplify configuration. The purpose of a "Fabric Model" is to keep the Operator in control of things like size of Kubernetes nodes or what SSH key pairs to assign to instances. Consider a scenario as an ops engineer where you want to allow developers to spin up very small `t2.nano` powered Kubernetes clusters for experimentation or CI tests without handing over full control or exposing unnecessary complexity. Let's create our first model which will be named `MyFirstFabricModel` and uses the domain name `mycompany.com`.

```bash
$> curl -X POST \
        -H "Content-Type: application/json" \
        -d '{"name": "myfirstmodel", "domain": "k736.net"}' \
        localhost:7000/models

200 OK
```

Once a specification is registered many clusters can reuse it!

### 3. Startup a Kubernetes Cluster

Let's startup a cluster!

```bash
$> curl -X POST \
        -H "Content-Type: application/json" \
        -d '{"name": "myfirstcluster", "model": "myfirstmodel"}' \
        localhost:7000/fabrics

200 OK!
```

### 4. Check for the cluster to come up (!! SKIP - NOT WORKING YET !!)

```bash
$> curl -H 'Accept: application/vnd.Fabric-v1+json' \
        localhost:5000/fabrics/myfirstcluster

{
  "name"           : "myfirstcluster",
  "creationTime"   : "2017-03-10'T'00:00:00",
  "activationTime" : null,
  "provider"       : "AWS",
  "status"         : "CREATION_IN_PROGRESS",
  "owner"          : "plombardi",
}
```

When `CREATION_IN_PROGRESS` changes to `CREATED` then you can start using your Kubernetes cluster.

### 5. Use your cluster

Once the status changes to `CREATED` you can start using the cluster! The first step to using your cluster is to get the [kubeconfig](https://kubernetes.io/docs/concepts/cluster-administration/authenticate-across-clusters-kubeconfig/) for the cluster:

```bash
$> mkdir   ~/.kube/config.d
$> curl --output ~/.kube/config.d/myfirstcluster \
        localhost:7000/fabrics/myfirstfabric/cluster/config
        
$> kubectl cluster-info --kubeconfig={$HOME}/.kube/config.d/myfirstcluster
Kubernetes master is running at https://api.myfirstcluster.example.org
KubeDNS is running at https://api.myfirstcluster.example.org/api/v1/proxy/namespaces/kube-system/services/kube-dns
```

**NOTE:** The `kubectl` command does not *yet* understand the `config.d` idiom, but there is a Pull Request moving along to enable this functionality in `kubectl`. The idea is that `kubectl` would load all the config files in this directory before use. Until then we need to simulate usage with the `--kubeconfig=<path>` option.

## Releases

### Versioning

Loom follows [Semantic Versioning 2.0](semver.org) for version numbers.

### Docker Images

Public docker images are published to [datawire/loom](https://hub.docker.com/r/datawire/hello/)

The tagging strategy for Docker images is described in the table below. The "pointer" column describes whether the image tag is stable or changing to point to the latest published image (e.g. `:latest`).

| Tag                              | When                                 | Pointer |
| -------------------------------- | ------------------------------------ | ------- |
| `:${Git-Commit-Hash}`            | Every successful build on any branch | No      |
| `:${Version}`                    | Every tag                            | No      |
| `:latest`                        | Every successful build               | Yes     |
| `:travis-${Travis-Build-Number}` | Every successful build on any branch | No      |

It is strongly recommend that production users use the `:${Version}` tag.

### Amazon Machine Image

Coming Soon

## License

Project is open-source software licensed under Apache 2.0. Please see [License](LICENSE) for further details.
