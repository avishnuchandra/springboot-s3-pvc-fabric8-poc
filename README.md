# springboot-s3-pvc-fabric8-poc

This repository is a proof-of-concept Spring Boot application that demonstrates integration with S3 and use of Persistent Volume Claims (PVC) for Kubernetes/OpenShift.

This project targets Java 21 and uses Gradle (Groovy DSL). The Docker image is built with a multi-stage build and the application uses the AWS SDK v2 for S3 interactions.

## Prerequisites

- Java 21 installed locally (for ./gradlew to run)
- Docker (for LocalStack or building the Docker image)
- kubectl and/or oc (OpenShift CLI) if deploying to a cluster
- An S3-compatible endpoint or AWS credentials for production

## Quick start - build

1. Build and run tests:

   ```bash
   ./gradlew clean build
   ```

2. Run the application locally (with real S3 credentials):

   ```bash
   export AWS_ACCESS_KEY_ID=YOUR_KEY
   export AWS_SECRET_ACCESS_KEY=YOUR_SECRET
   export AWS_REGION=us-east-1
   export S3_BUCKET=app-bucket
   
   ./gradlew bootRun
   ```

## Local development with LocalStack (S3 emulator)

1. Start LocalStack via docker-compose:

   ```bash
   docker-compose up -d
   ```

2. Create the S3 bucket (LocalStack exposes AWS CLI-compatible endpoint at http://localhost:4566):

   ```bash
   aws --endpoint-url=http://localhost:4566 s3 mb s3://app-bucket
   ```

3. Run the application pointing to LocalStack endpoint:

   ```bash
   export S3_ENDPOINT=http://localhost:4566
   export AWS_REGION=us-east-1
   export S3_BUCKET=app-bucket
   
   ./gradlew bootRun
   ```

## API

- `POST /files` (multipart/form-data, field name "file") - uploads a file to S3
- `GET /files/{key}` - downloads a file
- `GET /files/health` - health check

## Docker image

Build locally:

```bash
docker build -t ghcr.io/avishnuchandra/springboot-s3-pvc-fabric8-poc:latest .
```

## OpenShift / Kubernetes

Apply manifests in k8s/:

```bash
kubectl apply -f k8s/pvc.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

On OpenShift, create a route (if desired):

```bash
oc apply -f k8s/route.yaml
```

## CI

A GitHub Actions workflow is included at `.github/workflows/ci.yml` to build and test the project on push.

## Testing

The project includes unit and integration tests which use Testcontainers + LocalStack for S3 integration in CI. You must have Docker running locally for integration tests to work.

## Environment Variables

- `AWS_ACCESS_KEY_ID` - S3 access key (optional if using IAM roles)
- `AWS_SECRET_ACCESS_KEY` - S3 secret key (optional if using IAM roles)
- `AWS_REGION` - AWS region (default: us-east-1)
- `S3_ENDPOINT` - S3 endpoint override (use for LocalStack or S3-compatible services)
- `S3_BUCKET` - S3 bucket name (default: app-bucket)
