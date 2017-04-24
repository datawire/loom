# Loom

[![Build Status](https://travis-ci.org/datawire/loom.svg?branch=master)](https://travis-ci.org/datawire/loom)
[![Join the chat at https://gitter.im/datawire/loom](https://badges.gitter.im/datawire/loom.svg)](https://gitter.im/datawire/loom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.1.1-blue.svg)](https://kotlinlang.org/)

Loom enables operations engineers to provide a self-serve Kubernetes provisioning experience for developers and much more! Developers love Kubernetes, but it's a pain to get up and running on AWS and ops engineers usually have better things to be doing than babysitting devs as they get up and running with Kubernetes.

Thus we have Loom! Ops engineers install Loom inside of their AWS account as a persistent running server and developers use the simple HTTP API to self provision their own Kubernetes fabrics. Loom handles all the nitty gritty details of network creation, cluster setup and management.

## What is a Kubernetes "Fabric"?

When people talk about Kubernetes they usually talk purely in terms of the Kubernetes cluster where containers are scheduled and run. A "fabric" is an abstract concept that describes the entire ecosystem surrounding a Kubernetes cluster, for example, a "Kubernetes" and "AWS" fabric includes not only the Kubernetes cluster, but one or more VPC and within that VPC you may schedule non-Kubernetes resources to run such as RDS databases or EC2 instances. The point of calling it a "fabric" is that it's all nicely woven together for you so that, for example, containers running in the Kubernetes cluster can speak to the RDS databases without having to think about networking.

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

## License

Project is open-source software licensed under Apache 2.0. Please see [License](LICENSE) for further details.
