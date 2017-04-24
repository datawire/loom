---
layout: doc
weight: 1
title: "Why Loom?"
categories: about
---

You've adopted microservices. Your developers want to be able to create small, lightweight services whenever needed. These services need to run on Kubernetes, and they also need cloud resources such as databases or message queues.

# The painful way

You can make your developers learn CloudFormation, Terraform, Ansible, or some other code-as-infrastructure tool. Then, as part of the deployment process, your developer needs to write some code-as-infrastructure. Your developers will hate you.

# The slow way

Operations can create the necessary resources using CloudFormation, Terraform, Ansible, or some other tool. Developers can file tickets to tell ops what they need. This works, except it's slow. Your ops team needs to handle more tickets. Your developers will have to wait for ops.

# Using Loom

With Loom, operations code a `model` for resources that a developer uses (plus, Loom comes with some models to start with). Developers can then send a RESTful request to Loom to get the resources that they need. Self-service, here we come!
