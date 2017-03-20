// file: network.tf

// ---------------------------------------------------------------------------------------------------------------------
// Security Groups
//
// loom_frontend - Security group for the load balancer.
// loom_backend  - Security group for the Loom server(s).
// ---------------------------------------------------------------------------------------------------------------------

resource "aws_security_group" "loom_frontend" {
  name   = "loom-frontend"
  vpc_id = "${var.vpc_id}"
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
  vpc_id = "${var.vpc_id}"
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
  protocol          = -1
}

resource "aws_security_group_rule" "loom_backend_egress_all" {
  type              = "egress"
  security_group_id = "${aws_security_group.loom_backend.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

// ---------------------------------------------------------------------------------------------------------------------
// Load Balancer
// ---------------------------------------------------------------------------------------------------------------------
