FROM openjdk:8-alpine
MAINTAINER Datawire <dev@datawire.io>
LABEL PROJECT_REPO_URL         = "git@github.com:datawire/deployd.git" \
      PROJECT_REPO_BROWSER_URL = "https://github.com/datawire/deployd" \
      DESCRIPTION              = "Datawire Deployd" \
      VENDOR                   = "Datawire, Inc." \
      VENDOR_URL               = "https://datawire.io/"

ENV TERRAFORM_VERSION "0.9.4"
ENV TERRAFORM_SHA256  "cc1cffee3b82820b7f049bb290b841762ee920aef3cf4d95382cc7ea01135707"
ENV KOPS_VERSION      "1.6.0-beta.1"
ENV KOPS_SHA256       "84b6cc183c4924808babba602c534c83e052c48d845535d9ed72f45fe3da6185"

ARG JAR_NAME
ENV JAR_NAME ${JAR_NAME}

# Install System Dependencies
RUN apk --no-cache add curl unzip git

# Set WORKDIR to /service which is the root of all our apps.
WORKDIR /service

# Install application dependencies
#
# Terraform - for creating infrastructure and infrastructure-related services.
# Kops      - for managing Kubernetes clusters
# Kubectl   - for interacting with Kubernetes clusters
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
    && echo "${KOPS_SHA256}  /bin/kops" > kops_${KOPS_SHA256}_SHA256 \
    && sha256sum -c terraform_${TERRAFORM_VERSION}_SHA256 \
    && chmod +x /bin/kops /bin/kubectl

# COPY the app code and configuration into place then perform any final configuration steps.
COPY build/libs/${JAR_NAME} \
     src/main/shell/entrypoint-docker.sh \
     ./

COPY config/ config/

ENTRYPOINT ["./entrypoint-docker.sh"]
