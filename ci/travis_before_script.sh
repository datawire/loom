#!/usr/bin/env bash
set -euxo pipefail

pip install -Ur ci/requirements.txt
curl --output packer_${PACKER_VERSION}_linux_amd64.zip \
     https://releases.hashicorp.com/packer/${PACKER_VERSION}/packer_${PACKER_VERSION}_linux_amd64.zip

unzip packer_${PACKER_VERSION}_linux_amd64.zip -d /bin
rm -f packer_${PACKER_VERSION}_linux_amd64.zip

packer version
