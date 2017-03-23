// file: storage.tf

data "aws_caller_identity" "current" { }

resource "random_id" "auto_bucket_name" {
  byte_length = 4
  prefix      = "loom-${data.aws_caller_identity.current.account_id}-"
}

resource "aws_s3_bucket" "loom_storage" {
  bucket = "${length(var.state_storage_bucket) > 0 ? var.state_storage_bucket : random_id.auto_bucket_name.hex}"
  acl    = "private"

  tags {
    "io.datawire/Role" = "loom.state-storage"
  }
}
