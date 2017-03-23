# Loom

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
        -H "Content-Type: application/vnd.FabricModel-v1+json" \
        -d '{"name": "MyFirstFabricModel", "domain": "k736.net"}' \
        localhost:7000/fabric-models

200 OK
```

Once a specification is registered many clusters can reuse it!

### 3. Startup a Kubernetes Cluster

Let's bootup a cluster!

```bash
$> curl -X POST \
        -H "Content-Type: application/vnd.Fabric-v1+json" \
        -d '{"name": "myfirstcluster", "spec": "myfirstspec"}' \
        localhost:5000/fabrics

200 OK!
```

### 4. Check for the cluster to come up

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
$> curl -O ~/.kube/config.d/myfirstcluster \
        localhost:7000/fabrics/myfirstfabric/cluster/kubeconfig
        
$> kubectl cluster-info --kubeconfig={$HOME}/.kube/config.d/myfirstcluster
Kubernetes master is running at https://api.myfirstcluster.example.org
KubeDNS is running at https://api.myfirstcluster.example.org/api/v1/proxy/namespaces/kube-system/services/kube-dns
```

**NOTE:** The `kubectl` command does not *yet* understand the `config.d` idiom, but there is a Pull Request moving along to enable this functionality in `kubectl`. The idea is that `kubectl` would load all the config files in this directory before use. Until then we need to simulate usage with the `--kubeconfig=<path>` option.

## Loom API (OUT OF DATE)

The Loom API is composed of a handful of endpointss (this is just a rough sketch... don't fall in love with the impl).

### Create Fabric (POST: /fabrics)

A Fabric can be created by sending a JSON object described below to Loom:

**JSON**:

```json
{
  "name": "myfabric"
}
```

**cURL Request**

```bash
$> curl -X POST -H "Content-Type: application/vnd.FabricSpec-v1+json" -d '{"name": "myfabric"}' https://loom.example.org/fabrics

{
  "name"           : "myfabric",
  "creationTime"   : "2017-03-10'T'00:00:00",
  "activationTime" : null,
  "provider"       : "AWS",
  "status"         : "CREATION_IN_PROGRESS",
  "owner"          : "plombardi",
  "leaseDuration"  : "24h" # fabrics COULD have an expiration after which they would be cleaned automatically
}
```

### Get Fabric Information (GET: /fabrics/:name)

Information about a Fabric including its deployment status and the backing resource subnets can be queried just as easily:

**cURL Request**

```bash
$> curl -H "Accept: application/vnd.Fabric-v1+json" https://loom.example.org/fabrics/myfabric

HTTP 200 OK
Content-Type: application/vnd.Fabric-v1+json
{
  "name"           : "myfabric",
  "creationTime"   : "2017-03-10'T'00:00:00",
  "activationTime" : "2017-03-10'T'01:00:00",
  "provider"       : "AWS",
  "status"         : "ACTIVE",
  "owner"          : "plombardi",
  "locked"         : false,
  "network": {
    "id"                      : "vpc-xyzabc",
    "kubernetesSubnets"       : ["subnet-RTS", "subnet-QWE", "subnet-IOP"],
    "externalResourceSubnets" : ["subnet-123", "subnet-234", "subnet-xnt"]
  },
  "kubernetes": {
    "version"        : "1.6",
    "nodes"          : 10,
    "apiUrl"         : "https://api.myfabric.example.org",
    "dashboardUrl"   : "https://api.myfabric.example.org"
  }
}
```

### Delete Fabric (DELETE: /fabrics/:name)

A Fabric can be very easily cleaned up once it is done being used.

**cURL Request**

```bash
$> curl -X DELETE -H https://loom.example.org/fabrics/myname

HTTP 200 OK

-or-

HTTP 403 Forbidden (not allowed to do delete this fabric ... e.g. "prod")
```

### Upgrade or Modify a Fabric

A Fabric can be upgraded by sending an Upgrade request

**cURL Request**

```bash
$> curl -X PUT -d '{"version": "1.7"}' https://loom.example.org/fabrics/myname/updates

HTTP 200 OK

-or-

HTTP 403 Forbidden (not allowed to do delete this fabric ... e.g. "prod")
```

### Get a Fabric Model

<table>
  <tbody>
    <tr>
      <th>HTTP Method</th>
      <th>Path</th>
      <th>Parameters</th>
    </tr>
    <tr>
      <td>GET</td>
      <td>/fabric-models/:name</td>
      <td>
        <ul>
          <li>:name - the name of the fabric</li>
        </ul>
      </td>
    </tr>
  </tbody>
</table>

Inspect information about an existing fabric model:

**cURL Request**

```
$> curl /fabric-models/:name

{
  "name" : "MyFirstFabricModel",
  "allowedRegions" : [ "us-east-1" ],
  "version" : 1,
  "creationTime" : null,
  "domain" : "k736.net",
  "networking" : {
    "module" : "github.com/datawire/loom//src/terraform/network-v2"
  },
  "masterType" : "t2.nano",
  "nodeGroups" : [ 
    {
      "name" : "main",
      "nodeCount" : 1,
      "nodeType" : "t2.nano"
    } 
  ],
  "id" : "myfirstfabricmodel-v1"
}
```


## Installing Loom

Loom is easy to install on Amazon EC2. The provided installer will setup a robust, high availability Loom server for you and your team. To get started ensure you have your AWS credentials configure then run `./loom-up` and wait roughly five minutes for all the infrastructure to be properly provisioned. When this script is done you will be given an IP address for an Amazon ELB which you should assign an easy to remember DNS name to (e.g. loom.example.org).












