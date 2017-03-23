// file: variables.tf

variable "state_storage_bucket" {
  description = "The name of the state storage bucket (if null or empty then the name is auto-generated)."
  default     = ""
}

variable "vpc_cidr" {
  description = "The CIDR block assigned to the Loom VPC."
  default     = "10.0.0.0/16"
}

variable "ssh_key" {
  description = "The SSH key to use with the instance."
}

variable "image" {
  description = "The AMI identifier for loom."
  type = "map"
  default = {
    "us-east-1" = ""
  }
}

variable "instance_type" {
  description = "The machine/instance type for loom servers"
}