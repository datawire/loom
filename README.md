# Loom

[![Build Status](https://travis-ci.org/datawire/loom.svg?branch=master)](https://travis-ci.org/datawire/loom)
[![Join the chat at https://gitter.im/datawire/loom](https://badges.gitter.im/datawire/loom.svg)](https://gitter.im/datawire/loom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.1.1-blue.svg)](https://kotlinlang.org/)

Loom enables operations engineers to provide a self-serve Kubernetes provisioning experience for developers and much more! Developers love Kubernetes, but it's a pain to get up and running on AWS and ops engineers usually have better things to be doing than babysitting devs as they get up and running with Kubernetes.

Thus we have Loom! Ops engineers install Loom inside of their AWS account as a persistent running server and developers use the simple HTTP API to self provision their own Kubernetes fabrics. Loom handles all the nitty gritty details of network creation, cluster setup and management.

## What is a Kubernetes "Fabric"?

When people talk about Kubernetes they usually talk purely in terms of the Kubernetes cluster where containers are scheduled and run. A "fabric" is an abstract concept that describes the entire ecosystem surrounding a Kubernetes cluster, for example, a "Kubernetes" and "AWS" fabric includes not only the Kubernetes cluster, but one or more VPC and within that VPC you may schedule non-Kubernetes resources to run such as RDS databases or EC2 instances. The point of calling it a "fabric" is that it's all nicely woven together for you so that, for example, containers running in the Kubernetes cluster can speak to the RDS databases without having to think about networking.

## Getting Started in Five Minutes

This is a simple demonstration of Loom. For more detailed install instructions follow the [Detailed Install Guide](install/README.md).

### Prerequisites

- Access to an active pair of AWS credentials.
- [Docker](https://docker.io).
- A Route 53 controlled domain (e.g. `example.org`).

### 1. Run Loom

Loom is packaged as a [Docker](https://docker.com) image. It can be started with a `docker run ...` command shown below. When Loom runs for the first time it will create some necessary core infrastructure on your AWS account during a bootstrap phase:

- An AWS S3 bucket where Loom can store app state and config. The name will be `loom-state-${AWS_ACCOUNT_ID}`.
- An AWS DynamoDB table named `loom_terraform_state_lock` which is used internally when provisioning for state management safety against concurrent executions.

Depending on how you store your AWS credentials and config there are two common run options:

**Preferred: Use AWS credentials and config in `$HOME/.aws` directory**

`docker run -p 7000:7000 -v ${HOME}/.aws:/root/.aws --rm -it datawire/loom:alpha`

**Alternative: Set AWS environment variables**

```bash
docker run --rm -it \
  -p 7000:7000 \
  -e AWS_ACCESS_KEY_ID=<Your-AWS-API-Access-Key> \
  -e AWS_SECRET_ACCESS_KEY=<Your-AWS-API-Secret-Key> \
  -e AWS_REGION=us-east-1 \
  datawire/alpha
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

   `ssh-keygen -f ~/loom.key -t rsa -b 4096 -N ''
   
2. Create a new Fabric Model:

   ```bash
   curl -v -X POST \
        -H "Content-Type: application/json"
        -d '{"name": "simple", "version": 1, "domain": "${YOUR_DOMAIN}", "sshPublicKey": "'"$(cat ~/loom.key.pub)"'"}' \
        localhost:7000/models
   ```

Once a model is registered many clusters can reuse it!

### 3. Startup a Kubernetes Fabric

Let's start a fabric! Choose a name for your fabric, for example: `philsfab` or `testing`. We'll use the Model we setup in the previous step which 

```bash
curl -v -X POST \
     -H "Content-Type: application/json" \
     -d '{"name": "myfirstcluster", "model": "simple-v1"}' \
     localhost:7000/fabrics
```

### 4. Get your Kubernetes credentials

Talking to Kubernetes still requires credentials and you need Loom to give them to you:

```bash
mkdir ~/.kube/config.d
curl --output ~/.kube/config.d/myfirstcluster \
    localhost:7000/fabrics/myfirstfabric/cluster/config
```

### 5. Talking to Kubernetes

Loom lacks a fabric status API right now, but if you wait about 3 to 5 minutes (Go get a coffee!) after creating the cluster and getting your credentials then you should be able to do this:

```bash
kubectl cluster-info --kubeconfig={$HOME}/.kube/config.d/myfirstcluster
Kubernetes master is running at <Some-URL>
KubeDNS is running at <Some-URL>
```

Cluster up and running! Now you can use `kubectl` to actually do work with Kubernetes.

**NOTE:** The `kubectl` command does not *yet* understand the `config.d` idiom, but there is a Pull Request moving along to enable this functionality in `kubectl`. The idea is that `kubectl` would load all the config files in this directory before use. Until then we need to simulate usage with the `--kubeconfig=<path>` option.

## Releases

### Versioning

Loom follows [Semantic Versioning 2.0](semver.org) for version numbers.

### Docker Images

Public docker images are published to [datawire/loom](https://hub.docker.com/r/datawire/loom/)

The tagging strategy for Docker images is described in the table below. The "pointer" column describes whether the image tag is stable or changing to point to the latest published image (e.g. `:latest`).

| Tag                              | When                                 | Pointer |
| -------------------------------- | ------------------------------------ | ------- |
| `:${Git-Commit-Hash}`            | Every successful build on any branch | No      |
| `:${Version}`                    | Every tag                            | No      |
| `:latest`                        | Every successful build               | Yes     |
| `:${Branch-Name}`                | Every successful build on any branch | Yes     | 
| `:travis-${Travis-Build-Number}` | Every successful build on any branch | No      |

It is strongly recommend that production users use the `:${Version}` tag.

### Amazon Machine Image

Coming Soon

## License

Project is open-source software licensed under Apache 2.0. Please see [License](LICENSE) for further details.
