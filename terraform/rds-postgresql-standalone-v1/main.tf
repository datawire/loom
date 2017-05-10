// ---------------------------------------------------------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------------------------------------------------------

variable "vpc_id" {
  description = "The identifier of the containing VPC"
}

variable "db_name" {
  description = "Name of the database"
  default     = ""
}

variable "allocated_storage" {
  description = "Data storage capacity in gigabytes (e.g. 100)"
  default     = 100
}

variable "instance_size" {
  description = "The size of the database server"
  default     = "db.t2.micro"
}

variable "iops" {
  description = "Number of provisioned IOPS to assign to the PostgreSQL server"
  default = 0
}

variable "server_port" {
  description = "Port on which PostgreSQL server listens for connections"
  default     = 5432
}

variable "parameter_group_name" {
  description = "Database parameter group name"
  default     = ""
}

variable "publicly_accessible" {
  description = "Whether the server is publicly addressable or not"
  default     = true
}

variable "security_groups" {
  type = "list"
  description = "Security groups applied to the database instance"
}

variable "subnets" {
  type        = "list"
  description = "Subnets where database server instances can run"
}

// ---------------------------------------------------------------------------------------------------------------------
// Resources
// ---------------------------------------------------------------------------------------------------------------------

resource "random_id" "db_name" {
  byte_length = 8
  prefix      = "db"
}

resource "random_id" "db_password" {
  byte_length = 24
}

resource "aws_db_subnet_group" "main" {
  subnet_ids  = ["${var.subnets}"]
}

resource "aws_db_instance" "main" {
  identifier_prefix          = "pgsql-"
  allocated_storage          = "${var.allocated_storage}"
  auto_minor_version_upgrade = true
  skip_final_snapshot        = true
  engine                     = "postgres"
  engine_version             = "9.6.1"
  instance_class             = "${var.instance_size}"
  name                       = "${var.db_name != "" ? var.db_name : random_id.db_name.hex}"
  username                   = "${var.db_name != "" ? var.db_name : random_id.db_name.hex}"
  password                   = "${replace(random_id.db_password.b64, "/[_-]/", "")}"
  multi_az                   = true
  storage_type               = "${var.iops > 0 ? "io1" : "gp2"}"
  db_subnet_group_name       = "${aws_db_subnet_group.main.name}"
  publicly_accessible        = "${var.publicly_accessible}"
  parameter_group_name       = "${var.parameter_group_name != "" ? var.parameter_group_name : ""}"
  vpc_security_group_ids     = ["${var.security_groups}"]
}

output "db_username"    { value = "${aws_db_instance.main.username}" }
output "db_password"    { value = "${aws_db_instance.main.password}" }
output "db_address"     { value = "${aws_db_instance.main.address}" }
output "db_server_name" { value = "${aws_db_instance.main.name}" }
