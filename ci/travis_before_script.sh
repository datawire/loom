#!/usr/bin/env bash
set -euxo pipefail

BIN_INSTALL_DIR=${HOME}/bin
mkdir -p ${BIN_INSTALL_DIR}

# Install dependencies for python scripts used throughout CI
pip install -Ur ci/requirements.txt

# Install the Packer tool
curl --output packer_${PACKER_VERSION}_linux_amd64.zip \
     https://releases.hashicorp.com/packer/${PACKER_VERSION}/packer_${PACKER_VERSION}_linux_amd64.zip
unzip -o packer_${PACKER_VERSION}_linux_amd64.zip -d ${BIN_INSTALL_DIR}
rm -f packer_${PACKER_VERSION}_linux_amd64.zip

chmod 0755 ${BIN_INSTALL_DIR}/*

# Print out versions of binaries installed just for sanity checking
packer version
