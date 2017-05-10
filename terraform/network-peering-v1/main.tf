// networking-peering-v1/main.tf
//
// Implements a two-way peering relationship between source and target AWS VPC's.

// ---------------------------------------------------------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------------------------------------------------------

variable "source_vpc" {
  description = "source ('requestor') AWS VPC identifier."
}

variable "target_vpc" {
  description = "target ('acceptor') AWS VPC identifier."
}

variable "external_route_table" {
  description = "Route table identifier for the external subnets."
}

variable "internal_route_tables" {
  description = "Route table identifiers for internal subnets. There may be more than one as it is common to assign a NAT gateway to each subnet."
}

// ---------------------------------------------------------------------------------------------------------------------
// Data Lookups
// ---------------------------------------------------------------------------------------------------------------------

data "aws_vpc" "requestor" { id = "${var.source_vpc}" }
data "aws_vpc" "acceptor"  { id = "${var.target_vpc}" }

// ---------------------------------------------------------------------------------------------------------------------
// Resource Management
// ---------------------------------------------------------------------------------------------------------------------

resource "aws_vpc_peering_connection" "main" {
  vpc_id      = "${data.aws_vpc.requestor.id}"
  peer_vpc_id = "${data.aws_vpc.acceptor.id}"
  auto_accept = true
}

