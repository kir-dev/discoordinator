Discoordinator
===

## About

This is a web application with an integrated discord bot that is designed to give users discord roles based on our `authsch` oauth2 provider.

## Requirements

- Java 17

## Build

```bash
./gradlew assemble
```

## Build docker file

```bash
./gradlew buildDockerImage
```

## Publish to our registry

```bash
./gradlew pushDockerImage
```

You might first need to `docker login harbor.sch.bme.hu` with authsch username (without @...) and password.
You must be in the `K8S_NAMESPACE_org-kir-prod` AD group.

