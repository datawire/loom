// file: network.tf

data "aws_availability_zones" "available" { }

resource "aws_vpc" "loom" {
  cidr_block           = "${var.vpc_cidr}"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags {
    Name = "loom"
    "io.datawire/Role" = "loom"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = "${aws_vpc.loom.id}"

  tags {
    Name = "loom"
    "io.datawire/Role" = "loom"
  }
}

resource "aws_subnet" "external" {
  vpc_id                  = "${aws_vpc.loom.id}"
  cidr_block              = "${cidrsubnet(aws_vpc.loom.cidr_block, 6}"
  availability_zone       = "${element(sort(data.aws_availability_zones.available.names), count.index)}"
  count                   = "${length(slice(data.aws_availability_zones.available.names, 0, 3))}"
  map_public_ip_on_launch = true

  tags {
    Name = "loom"
    "io.datawire/Role" = "loom"
  }
}

resource "aws_route_table" "external" {
  vpc_id = "${aws_vpc.loom.id}"

  tags {
    Name = "loom-external-001"
  }
}

resource "aws_route" "external" {
  route_table_id         = "${aws_route_table.external.id}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = "${aws_internet_gateway.main.id}"
}

resource "aws_route_table_association" "external" {
  count          = "${aws_subnet.external.count}"
  subnet_id      = "${element(aws_subnet.external.*.id, count.index)}"
  route_table_id = "${aws_route_table.external.id}"
}

// ---------------
// Security Groups
// ---------------

resource "aws_security_group" "loom_frontend" {
  name   = "loom-frontend"
  vpc_id = "${aws_vpc.loom.id}"
}

resource "aws_security_group_rule" "loom_frontend_ingress_tcp80" {
  type              = "ingress"
  security_group_id = "${aws_security_group.loom_frontend.id}"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "loom_frontend_ingress_tcp443" {
  type              = "ingress"
  security_group_id = "${aws_security_group.loom_frontend.id}"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "loom_frontend_egress_all" {
  type              = "egress"
  security_group_id = "${aws_security_group.loom_frontend.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group" "loom_backend" {
  name   = "loom-backend"
  vpc_id = "${aws_vpc.loom.id}"
}

resource "aws_security_group_rule" "loom_backend_ingress_tcp80" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.loom_backend.id}"
  from_port                = 80
  to_port                  = 80
  protocol                 = "tcp"
  source_security_group_id = "${aws_security_group.loom_frontend.id}"
}

resource "aws_security_group_rule" "loom_backend_ingress_self_all" {
  type              = "ingress"
  security_group_id = "${aws_security_group.loom_backend.id}"
  self              = true
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
}

resource "aws_security_group_rule" "loom_backend_egress_all" {
  type              = "egress"
  security_group_id = "${aws_security_group.loom_backend.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}
