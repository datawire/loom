// file: src/terraform/network_v2/main.tf

// ---------------------------------------------------------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------------------------------------------------------

variable "cidr_block" {
  description = "CIDR block for the VPC"
}

variable "name" {
  description = "VPC name (e.g. myvpc)"
}

data "aws_availability_zones" "available" { }

// ---------------------------------------------------------------------------------------------------------------------
// Resources
// ---------------------------------------------------------------------------------------------------------------------

resource "aws_vpc" "main" {
  cidr_block           = "${var.cidr_block}"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags {
    Name = "${var.name}"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = "${aws_vpc.main.id}"

  tags {
    Name = "${var.name}"
  }
}

resource "aws_route_table" "external" {
  vpc_id = "${aws_vpc.main.id}"

  tags {
    Name = "${var.name}-external-001"
  }
}

resource "aws_route" "external" {
  route_table_id         = "${aws_route_table.external.id}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = "${aws_internet_gateway.main.id}"
}

resource "aws_route_table" "internal" {
  count  = "${length(data.aws_availability_zones.available.names)}"
  vpc_id = "${aws_vpc.main.id}"

  tags {
    Name = "${var.name}-${format("internal-%03d", count.index + 1)}"
  }
}

resource "aws_security_group" "main" {
  vpc_id      = "${aws_vpc.main.id}"
  name_prefix = "main-"

  ingress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    self      = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

// ---------------------------------------------------------------------------------------------------------------------
// Outputs
// ---------------------------------------------------------------------------------------------------------------------

output "id"                 { value = "${aws_vpc.main.id}" }
output "cidr_block"         { value = "${aws_vpc.main.cidr_block}" }
output "availability_zones" { value = ["${slice(data.aws_availability_zones.available.names, 0, 3)}"] }
