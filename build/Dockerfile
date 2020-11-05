# Copyright 2020 Intel Corporation
# SPDX-License-Identifier: Apache 2.0

FROM ubuntu:20.04

RUN apt-get update && \
    apt-get install -y git  \
    openjdk-11-jdk-headless \
    maven

# Create a user 'sdouser'. If the user name is updated, please update the same in docker-compose.yaml.
RUN useradd -ms /bin/bash sdouser
RUN mkdir -p /home/sdouser/supply-chain-tools/ ; chown -R sdouser:sdouser /home/sdouser/supply-chain-tools/
USER sdouser

WORKDIR /home/sdouser/supply-chain-tools/
ENTRYPOINT ./build/build.sh
