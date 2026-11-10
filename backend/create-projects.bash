#!/usr/bin/env bash
mkdir microservices
cd microservices

/d/spring-boot-cli-2.7.1-bin/spring-2.7.1/bin/spring init \
--boot-version=2.7.1 \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=product-service \
--package-name=com.light.microservices.core.product \
--groupId=com.light.microservices.core.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-service

/d/spring-boot-cli-2.7.1-bin/spring-2.7.1/bin/spring init \
--boot-version=2.7.1 \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=review-service \
--package-name=com.light.microservices.core.review \
--groupId=com.light.microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

/d/spring-boot-cli-2.7.1-bin/spring-2.7.1/bin/spring init \
--boot-version=2.7.1 \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=recommendation-service \
--package-name=com.light.microservices.core.recommendation \
--groupId=com.light.microservices.core.recommendation \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
recommendation-service

/d/spring-boot-cli-2.7.1-bin/spring-2.7.1/bin/spring init \
--boot-version=2.7.1 \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=product-composite-service \
--package-name=com.light.microservices.composite.product \
--groupId=com.light.microservices.composite.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-composite-service

cd ..
