resource "random_pet" "main" {
  length    = 2
  separator = "-"
}

output "pet_name" { value = "${random_pet.main.id}" }