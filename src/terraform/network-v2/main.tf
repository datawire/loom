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

resource "aws_subnet" "internal" {
  vpc_id            = "${aws_vpc.main.id}"
  cidr_block        = "${cidrsubnet(var.cidr_block, 4, count.index)}"
  availability_zone = "${element(sort(data.aws_availability_zones.available.names), count.index)}"
  count             = "${length(slice(data.aws_availability_zones.available.names, 0, 3))}"

  tags {
    Name = "${var.name}-${format("internal-%03d", count.index + 1)}"
  }
}

resource "aws_subnet" "external" {
  vpc_id                  = "${aws_vpc.main.id}"
  cidr_block              = "${cidrsubnet(var.cidr_block, 4, (count.index + aws_subnet.internal.count + 1))}"
  availability_zone       = "${element(sort(data.aws_availability_zones.available.names), count.index)}"
  count                   = "${length(slice(data.aws_availability_zones.available.names, 0, 3))}"
  map_public_ip_on_launch = true

  tags {
    Name = "${var.name}-${format("external-%03d", count.index + 1)}"
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

output "id"                 { value = "${aws_vpc.main.id}" }
output "cidr_block"         { value = "${aws_vpc.main.cidr_block}" }
output "external_subets"    { value = "${aws_subnet.external.*}" }
