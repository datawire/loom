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

resource "aws_subnet" "internal" {
  vpc_id            = "${aws_vpc.main.id}"
  cidr_block        = "${cidrsubnet(var.cidr_block, 4, count.index)}"
  availability_zone = "${element(sort(data.aws_availability_zones.available.names), count.index)}"
  count             = "${length(data.aws_availability_zones.available.names)}"

  tags {
    Name = "${var.name}-${format("internal-%03d", count.index + 1)}"
    Tier = "internal"
  }
}

resource "aws_subnet" "external" {
  vpc_id                  = "${aws_vpc.main.id}"
  cidr_block              = "${cidrsubnet(var.cidr_block, 4, length(aws_subnet.internal.*.id) + count.index)}"
  availability_zone       = "${element(sort(data.aws_availability_zones.available.names), count.index)}"
  count                   = "${length(data.aws_availability_zones.available.names)}"
  map_public_ip_on_launch = true

  tags {
    Name = "${var.name}-${format("external-%03d", count.index + 1)}"
    Tier = "external"
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

resource "aws_default_security_group" "main" {
  vpc_id      = "${aws_vpc.main.id}"

  tags {
    Name = "${var.name}-main"
    Tier = "all"
  }
}

resource "aws_security_group_rule" "ingress_self_all" {
  type              = "ingress"
  security_group_id = "${aws_vpc.main.default_security_group_id}"
  self              = true
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
}

resource "aws_security_group_rule" "egress_all" {
  type              = "egress"
  security_group_id = "${aws_vpc.main.default_security_group_id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_route_table_association" "internal" {
  count          = "${aws_subnet.internal.count}"
  subnet_id      = "${element(aws_subnet.internal.*.id, count.index)}"
  route_table_id = "${element(aws_route_table.internal.*.id, count.index)}"
}

resource "aws_route_table_association" "external" {
  count          = "${aws_subnet.external.count}"
  subnet_id      = "${element(aws_subnet.external.*.id, count.index)}"
  route_table_id = "${aws_route_table.external.id}"
}

// ---------------------------------------------------------------------------------------------------------------------
// Outputs
// ---------------------------------------------------------------------------------------------------------------------

output "vpc_id"                     { value = "${aws_vpc.main.id}" }
output "cidr_block"                 { value = "${aws_vpc.main.cidr_block}" }
output "external_subnets"           { value = ["${aws_subnet.external.*.id}"] }
output "internal_subnets"           { value = ["${aws_subnet.internal.*.id}"] }
output "availability_zones"         { value = ["${data.aws_availability_zones.available.names}"] }
output "main_security_group"        { value = "${aws_default_security_group.main.id}" }
output "external_route_table_id"    { value = "${aws_route_table.external.id}" }
output "internal_route_table_ids"   { value = ["${aws_route_table.internal.*.id}"] }
output "internal_route_table_count" { value = "${aws_route_table.internal.count}" }
