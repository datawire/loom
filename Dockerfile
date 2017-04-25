FROM openjdk:8-alpine
MAINTAINER Datawire <dev@datawire.io>
LABEL PROJECT_REPO_URL         = "git@github.com:datawire/deployd.git" \
      PROJECT_REPO_BROWSER_URL = "https://github.com/datawire/deployd" \
      DESCRIPTION              = "Datawire Deployd" \
      VENDOR                   = "Datawire, Inc." \
      VENDOR_URL               = "https://datawire.io/"

ENV TERRAFORM_VERSION "0.9.3"
ENV TERRAFORM_SHA256  "f34b96f7b7edaf8c4dc65f6164ba0b8f21195f5cbe5b7288ad994aa9794bb607"
ENV KOPS_VERSION      "1.5.3"

ARG IMPL_VERSION
ENV IMPL_VERSION ${IMPL_VERSION}

# Install System Dependencies
#
#
RUN apk --no-cache add curl unzip git

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
    && sha256sum -c terraform_${TERRAFORM_VERSION}_SHA256 \
    && unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip -d /bin \
    && rm -f terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
    && curl --output /tmp/kubectl_version https://storage.googleapis.com/kubernetes-release/release/stable.txt \
    && curl --output /bin/kubectl \
        "https://storage.googleapis.com/kubernetes-release/release/$(cat /tmp/kubectl_version)/bin/linux/amd64/kubectl" \
    && curl -L --output /bin/kops \
        "https://github.com/kubernetes/kops/releases/download/${KOPS_VERSION}/kops-linux-amd64" \
    && chmod +x /bin/kops /bin/kubectl

# COPY the app code and configuration into place then perform any final configuration steps.
COPY build/libs/loom-${IMPL_VERSION}-fat.jar \
     src/main/shell/entrypoint-docker.sh \
     ./

COPY config/ config/

ENTRYPOINT ["./entrypoint-docker.sh"]
