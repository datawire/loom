#!/usr/bin/env bash
set -euo pipefail

BIN_INSTALL_DIR="/usr/local/bin"

TERRAFORM_VERSION="0.9.3"
KOPS_VERSION="1.5.3"

msg() {
  content="${1:?Message content not specified!}"
  printf "%s\n" "==> $content"
}

msg "Bake started!"

msg "Performing System Update"
yum -y update

msg "Install OpenJDK JRE and other system dependencies"
yum -y install \
    curl \
    java-1.8.0-openjdk \
    unzip \
    wget

msg "Install Terraform"
curl --output terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
    https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip

unzip -o terraform_${TERRAFORM_VERSION}_linux_amd64.zip -d ${BIN_INSTALL_DIR}
chmod 0755 ${BIN_INSTALL_DIR}/terraform

rm -f terraform_${TERRAFORM_VERSION}_linux_amd64.zip

msg "Install Kops"
curl -L --output ${BIN_INSTALL_DIR}/kops \
        "https://github.com/kubernetes/kops/releases/download/${KOPS_VERSION}/kops-linux-amd64" \

chmod 0755 ${BIN_INSTALL_DIR}/terraform

msg "Install Kubectl"
curl --output /tmp/kubectl_version https://storage.googleapis.com/kubernetes-release/release/stable.txt
curl --output ${BIN_INSTALL_DIR}/kubectl \
     "https://storage.googleapis.com/kubernetes-release/release/$(cat /tmp/kubectl_version)/bin/linux/amd64/kubectl"

chmod 0755 ${BIN_INSTALL_DIR}/terraform
rm -f /tmp/kubectl_version

msg "Install Loom"

# TODO(plombardi): Install Loom from GitHub release.

msg "Shredding SSH keys..."
rm -f /etc/ssh/*_key /etc/ssh/*_key.pub

msg "Bake completed!"
