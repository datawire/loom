FROM datawire/ubuntu-java8:6d3e7adaa2
MAINTAINER Datawire <dev@datawire.io>
LABEL PROJECT_REPO_URL         = "git@github.com:datawire/deployd.git" \
      PROJECT_REPO_BROWSER_URL = "https://github.com/datawire/deployd" \
      DESCRIPTION              = "Datawire Deployd" \
      VENDOR                   = "Datawire, Inc." \
      VENDOR_URL               = "https://datawire.io/"

ENV TERRAFORM_VERSION "0.9.1"
ENV TERRAFORM_SHA256  "b3b18a719258dcc02b7b972eedf417be0b497e4129063711bca82877dbe65553"
ENV KOPS_VERSION      "1.5.3"

ARG IMPL_VERSION
ENV IMPL_VERSION ${IMPL_VERSION}

# Install System Dependencies
#
#
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        unzip \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Set WORKDIR to /service which is the root of all our apps.
WORKDIR /service

# Install application dependencies
#
# Terraform - for creating remote infrastructure.
# Kops      - for managing remote clusters.
#
RUN curl --output terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
        https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
    && echo "${TERRAFORM_SHA256}  terraform_${TERRAFORM_VERSION}_linux_amd64.zip" > terraform_${TERRAFORM_VERSION}_SHA256 \
    && sha256sum -c --strict terraform_${TERRAFORM_VERSION}_SHA256 \
    && unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip -d bin/ \
    && rm -f terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
    && curl --output /bin/kops \
        "https://github.com/kubernetes/kops/releases/download/${KOPS_VERSION}/kops-linux-amd64"

# COPY the app code and configuration into place then perform any final configuration steps.
COPY build/libs/loom-${IMPL_VERSION}-fat.jar \
     src/main/shell/entrypoint-docker.sh \
     ./

ENTRYPOINT ["./entrypoint-docker.sh"]