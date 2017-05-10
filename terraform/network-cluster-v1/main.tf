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

resource "aws_security_group" "main" {
  vpc_id      = "${aws_vpc.main.id}"
  name_prefix = "main-"
  description = "main security group"

  tags {
    Tier = "all"
  }
}

resource "aws_security_group_rule" "ingress_self_all" {
  type              = "ingress"
  security_group_id = "${aws_security_group.main.id}"
  self              = true
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
}

resource "aws_security_group_rule" "egress_all" {
  type              = "egress"
  security_group_id = "${aws_security_group.main.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

// ---------------------------------------------------------------------------------------------------------------------
// Outputs
// ---------------------------------------------------------------------------------------------------------------------

output "id"                  { value = "${aws_vpc.main.id}" }
output "cidr_block"          { value = "${aws_vpc.main.cidr_block}" }
output "availability_zones"  { value = ["${data.aws_availability_zones.available.names}"] }
output "main_security_group" { value = "${aws_security_group.main.id}" }
