// file: main.tf

variable "vpc_id" {
  description = "Identifier of the Virtual Private Cloud to run Loom inside"
}

variable "subnets" {
  description = "Identifiers of subnets where Loom compute and network components are provisioned"
}

variable "instance_type" {
  description = "Type of AWS instance to run Loom on"
  default     = "t2.nano"
}

data "aws_ami" "loom_image" {
  most_recent = true

  filter {
    name   = "owner-alias"
    values = ["datawire"]
  }
}

// ---------------------------------------------------------------------------------------------------------------------
// IAM
// ---------------------------------------------------------------------------------------------------------------------


// ---------------------------------------------------------------------------------------------------------------------
// Loom Compute Resources
// ---------------------------------------------------------------------------------------------------------------------

resource "aws_security_group" "main" {

  egress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
  }

  ingress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = 80
    protocol    = "tcp"
    to_port     = 80
  }

  ingress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = 443
    protocol    = "tcp"
    to_port     = 443
  }

  name = "${var.environment_type}-${var.service_namespace}-${var.service_name}-lb"
  description = "web to load balancer"

  tags {
    Environment = "${var.environment_type}"
    Role        = "dwc:networking"
    Service     = "${var.service_namespace}-${var.service_name}"
  }

  vpc_id = "${terraform_remote_state.foundation.output.vpc_id}"
}

resource "aws_alb" "loom" {
  name            = "loom-lb"
  internal        = false
  security_groups = ["${aws_security_group.alb_sg.id}"]
  subnets         = ["${aws_subnet.public.*.id}"]

  enable_deletion_protection = true

  access_logs {
    bucket = "${aws_s3_bucket.alb_logs.bucket}"
    prefix = "test-alb"
  }

  tags {
    Environment = "production"
  }
}

resource "aws_launch_configuration" "cluster" {
  associate_public_ip_address = true
  enable_monitoring           = true
  iam_instance_profile        = "${var.instance_profile}"
  image_id                    = "${data.aws_ami.loom_image.image_id}"
  instance_type               = "${var.instance_type}"
  key_name                    = "${var.ssh_key}"
  name_prefix                 = "${var.environment_type}-${var.service_namespace}-${var.service_name}-${var.cluster_color}-"
  security_groups             = ["${compact(split(",", "${aws_security_group.cluster_group.id},${var.cluster_additional_security_groups}"))}"]

  lifecycle { create_before_destroy = true }
}

resource "aws_autoscaling_group" "cluster" {
  force_delete                = false
  health_check_grace_period   = "${var.cluster_health_check_grace_period}"
  health_check_type           = "ELB"
  launch_configuration        = "${aws_launch_configuration.cluster.id}"
  load_balancers              = ["${var.cluster_load_balancer}"]
  max_size                    = "${var.cluster_max_size}"
  min_size                    = "${var.cluster_min_size}"
  name                        = "${var.environment_type}-${var.service_namespace}-${var.service_name}-${var.cluster_color}"
  vpc_zone_identifier         = ["${split(",", terraform_remote_state.foundation.output.public_subnets)}"]
  wait_for_capacity_timeout   = "${var.cluster_wait_for_capacity_timeout}"
  wait_for_elb_capacity       = "${var.cluster_min_size}"

  tag {
    key                 = "Name",
    value               = "Datawire Loom",
    propagate_at_launch = true
  }

  tag {
    key                 = "Vendor"
    value               = "Datawire Inc."
    propagate_at_launch = true
  }
}