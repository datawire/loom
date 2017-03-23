# Loom

Loom is used to setup self-serve Kubernetes fabrics on Amazon Web Services with an experience that is similar to Google Container Engine for Google Cloud Platform. Developers love Kubernetes, but it's a pain to get up and running on AWS and Operations engineers have better things to be doing than babysitting developers as they get up and running with Kubernetes.

Thus we have Loom! Operators install Loom inside of their AWS account as a persistent running server and developers use the simple HTTP API to self provision their own Kubernetes clusters.

## Getting Started in Five Minutes

This is a simple demonstration about Loom. For more detailed install instructions follow the [Detailed Install Guide](install/README.md).

### 1. Run Loom

Loom server is packaged as a Docker image. Start it locally:

```
$> docker run --rm -it -p 5000:5000 datawire/loom:0.1.0

Loom has started! API => localhost:5000
```

### 2. Define a Fabric Model

A fabric specification is a reusable template and configuration for all clusters. As an ops engineer you want to allow developers to spin up very small `t2.nano` powered Kubernetes clusters during CI tests without handing over full control or exposing unnecessary complexity. Let's create our first spec which will be named `myfirstspec` and uses the domain name `mycompany.com`.

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












