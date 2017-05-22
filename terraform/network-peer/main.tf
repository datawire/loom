// ---------------------------------------------------------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------------------------------------------------------

variable "external_services_vpc" {
  description = "VPC that contains the external (non-kubernetes) services."
}

variable "external_services_vpc_cidr" {
  description = "VPC CIDR block for the external (non-kubernetes) services."
}

variable "external_services_vpc_external_route_table" {
  description = "route table identifier for the external services external (public) route table"
}

variable "external_services_vpc_internal_route_tables" {
  type        = "list"
  description = "route table identifiers for the external services internal (public) route table"
}

variable "external_services_vpc_internal_route_tables_count" {
  description = "count of route table identifiers for the external services external (public) route table"
}

variable "kubernetes_vpc" {
  description = "VPC that contains the Kubernetes cluster nodes."
}

variable "kubernetes_vpc_cidr" {
  description = "VPC CIDR block for the Kubernetes cluster nodes."
}

variable "kubernetes_vpc_route_table" {
  description = "route table identifier for the Kubernetes cluster."
}

// ---------------------------------------------------------------------------------------------------------------------
// Resource Management
// ---------------------------------------------------------------------------------------------------------------------

resource "aws_vpc_peering_connection" "main" {
  vpc_id      = "${var.external_services_vpc}" // "Requestor"
  peer_vpc_id = "${var.kubernetes_vpc}"        // "Acceptor"
  auto_accept = true

  accepter {
    allow_remote_vpc_dns_resolution = true
  }

  requester {
    allow_remote_vpc_dns_resolution = true
  }
}

resource "aws_route" "kubernetes-external_services" {
  route_table_id            = "${var.kubernetes_vpc_route_table}"
  destination_cidr_block    = "${var.external_services_vpc_cidr}"
  vpc_peering_connection_id = "${aws_vpc_peering_connection.main.id}"
}

resource "aws_route" "external_services_external-kubernetes" {
  route_table_id            = "${var.external_services_vpc_external_route_table}"
  destination_cidr_block    = "${var.kubernetes_vpc_cidr}"
  vpc_peering_connection_id = "${aws_vpc_peering_connection.main.id}"
}

resource "aws_route" "external_services_internal-kubernetes" {
  count                     = "${var.external_services_vpc_internal_route_tables_count}"
  route_table_id            = "${element(var.external_services_vpc_internal_route_tables, count.index)}"
  destination_cidr_block    = "${var.kubernetes_vpc_cidr}"
  vpc_peering_connection_id = "${aws_vpc_peering_connection.main.id}"
}
