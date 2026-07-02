# Objective

Build a production-quality Spring Boot application to verify connectivity with:

- AWS S3
- OpenShift PVC
- Fabric8 Kubernetes Client

The application will later become the base for a Spring Batch processing service.

## Profiles

local

openshift

## S3

Support private buckets.

Operations

- List Buckets
- List Objects
- Upload
- Download
- Delete
- Health Check

Use AWS SDK v2.

Credentials from environment variables.

Support LocalStack.

## PVC

Mount path

/mnt/data

Operations

- Write
- Read
- Append
- Delete
- List

Health endpoint.

## Fabric8

Operations

- List Pods
- List Jobs
- Create Job
- Delete Job
- Current Namespace

Health endpoint.

Automatically use ServiceAccount inside OpenShift.

Outside OpenShift use kubeconfig.

## REST APIs

GET /health/all

GET /s3/buckets

GET /s3/files

POST /s3/upload

GET /s3/download

DELETE /s3/delete

POST /pvc/write

GET /pvc/read

GET /pvc/files

DELETE /pvc/delete

GET /k8s/pods

GET /k8s/jobs

POST /k8s/jobs

DELETE /k8s/jobs/{name}

## OpenShift

Generate

Deployment

Service

Route

PVC

ConfigMap

Secret example

Role

RoleBinding

ServiceAccount

Resource Limits

Liveness

Readiness

## Docker

Dockerfile

docker-compose.yml

LocalStack

README

Postman Collection

Application should compile without errors.

No placeholders.