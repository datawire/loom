# Loom

Loom is fabric creation and management tool for Kubernetes on AWS (initially). A Loom install is a simple server that runs inside of an EC2 instance or ECS container. A running loom server exposes a simple HTTP API that be easily scripted against for provisioning Kubernetes-centric fabrics.

## Rationale

Amazon Web Services ("AWS") lacks a 1st-party Kubernetes equivalent to Google Cloud Platform's Container Engine and the 3rd party tools that exist for provisioning Kubernetes clusters on AWS such as Kubernetes Ops (`kops`) and (`kube-aws`) are heavily geared towards operations users. Under the hood Loom is really just orchestrating these tools and others in a user-friendly fashion.

Along with managing the underlying Kubernetes cluster's creation Loom will also ensure that the fabric as a whole is setup properly which means manging specialized subnets for backing resources such as AWS RDS instances.

The theory behind Loom is that an operations-savvy user:

1. Installs Loom and assigns a DNS name to Loom (e.g. loom.example.org)
2. Tells developers and operations engineers alike to get, manage and delete Kubernetes-centric fabrics via the Loom API.

## Loom's Target Audience

Loom is primarily focused at an operations engineer that:

- Wants to provide a Google Container Engine like provisioning experience for their developers, for example, so devs can run sandboxed Kubernetes environments for themselves or provision a Kubernetes cluster for automated tests.
- Needs to manage multiple Kubernetes clusters.

## Loom API

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

## Installing Loom

Loom is easy to install on Amazon EC2. The provided installer will setup a robust, high availability Loom server for you and your team. To get started ensure you have your AWS credentials configure then run `./loom-up` and wait roughly five minutes for all the infrastructure to be properly provisioned. When this script is done you will be given an IP address for an Amazon ELB which you should assign an easy to remember DNS name to (e.g. loom.example.org).












