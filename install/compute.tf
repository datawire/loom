// file: compute.tf

resource "aws_elb" "loom_frontend" {
  cross_zone_load_balancing = true
  internal                  = false
  name                      = "loom-frontend"
  security_groups           = ["${aws_security_group.loom_frontend.id}"]
  subnets                   = ["${aws_subnet.external.*.id}"]

  listener {
    lb_port            = 443
    lb_protocol        = "HTTPS"
    instance_port      = 80
    instance_protocol  = "HTTP"
  }

  health_check {
    healthy_threshold   = 3
    interval            = 10
    target              = "HTTP:80/health"
    timeout             = 5
    unhealthy_threshold = 3
  }
}

resource "aws_launch_configuration" "loom" {
  name_prefix                 = "loom-"
  associate_public_ip_address = true
  iam_instance_profile        = "${}"
  image_id                    = "${lookup(var.image, data.aws_region.current)}"
  instance_type               = "${var.instance_type}"
  key_name                    = "${var.ssh_key}"
  security_groups             = ["${aws_security_group.loom_backend.id}"]
  enable_monitoring           = true
  ebs_optimized               = true

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "loom" {
  health_check_grace_period = 300
  health_check_type         = "ELB"
  launch_configuration      = "${aws_launch_configuration.loom.name}"
  load_balancers            = ["${aws_elb.loom_frontend.name}"]
  vpc_zone_identifier       = ["${aws_subnet.external.*.id}"]
  min_size                  = 1
  max_size                  = 2
  wait_for_capacity_timeout = 10
  wait_for_elb_capacity     = 1

  lifecycle {
    create_before_destroy = true
  }

  tag {
    key                 = "Name"
    value               = "Loom"
    propagate_at_launch = true
  }
}