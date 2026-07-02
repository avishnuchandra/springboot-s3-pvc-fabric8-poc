# springboot-s3-pvc-fabric8-poc

A production-quality Spring Boot application that demonstrates integration with AWS S3, Kubernetes Persistent Volume Claims (PVC), and Kubernetes/OpenShift orchestration via Fabric8.

## Features

- **S3 Integration**: List buckets, upload, download, delete objects, support for LocalStack
- **PVC Operations**: Write, read, append, delete files on mounted persistent volumes
- **Kubernetes Management**: List/create/delete pods and jobs using Fabric8 client
- **Health Checks**: Aggregated health endpoint for all services
- **RBAC Support**: ServiceAccount, Role, RoleBinding manifests for secure Kubernetes deployment
- **Java 21**: Modern JVM with Spring Boot 3.2.4
- **Comprehensive Testing**: Unit and integration tests with Testcontainers + LocalStack

## Prerequisites

- Java 21 installed locally
- Docker (for LocalStack, building images)
- kubectl and/or oc (OpenShift CLI) for cluster deployments
- AWS credentials or LocalStack endpoint for S3 development

## Quick Start - Local Development

### 1. Build the project

```bash
./gradlew clean build
```

### 2. Run tests

```bash
./gradlew test
```

### 3. Run with LocalStack (S3 emulation)

Start LocalStack:

```bash
docker-compose up -d
```

Create S3 bucket:

```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://app-bucket
```

Run the application:

```bash
export S3_ENDPOINT=http://localhost:4566
export AWS_REGION=us-east-1
export S3_BUCKET=app-bucket
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

./gradlew bootRun
```

### 4. Test the APIs

Use the included Postman collection (`postman-collection.json`) or curl:

```bash
# Health check
curl http://localhost:8080/health/all

# List buckets
curl http://localhost:8080/s3/buckets

# Upload file
curl -F "file=@myfile.txt" http://localhost:8080/s3/upload

# List PVC files
curl http://localhost:8080/pvc/files
```

## API Documentation

### Health Endpoints

- `GET /health/all` - Aggregated health check for S3, PVC, Kubernetes
- `GET /s3/health` - S3 service health
- `GET /pvc/health` - PVC service health
- `GET /k8s/health` - Kubernetes service health

### S3 Operations

- `GET /s3/buckets` - List all S3 buckets
- `GET /s3/files?bucket=BUCKET_NAME` - List objects in a bucket
- `POST /s3/upload` - Upload a file (multipart/form-data)
- `GET /s3/download/{key}` - Download a file
- `DELETE /s3/delete/{key}` - Delete a file from S3

### PVC Operations (mounted at `/mnt/data`)

- `POST /pvc/write` - Write a file to PVC (multipart/form-data)
- `GET /pvc/read/{filename}` - Read a file from PVC
- `GET /pvc/files` - List files in PVC
- `POST /pvc/append/{filename}` - Append to a file (multipart/form-data)
- `DELETE /pvc/delete/{filename}` - Delete a file from PVC

### Kubernetes Operations

- `GET /k8s/pods?namespace=NAMESPACE` - List pods
- `GET /k8s/jobs?namespace=NAMESPACE` - List jobs
- `POST /k8s/jobs?namespace=NAMESPACE` - Create a job (JSON body)
- `DELETE /k8s/jobs/{name}?namespace=NAMESPACE` - Delete a job
- `GET /k8s/namespace` - Get current namespace

## Docker Image

### Build locally

```bash
docker build -t ghcr.io/avishnuchandra/springboot-s3-pvc-fabric8-poc:latest .
```

### Run container

```bash
docker run -e S3_ENDPOINT=http://host.docker.internal:4566 \
  -e S3_BUCKET=app-bucket \
  -e AWS_ACCESS_KEY_ID=test \
  -e AWS_SECRET_ACCESS_KEY=test \
  -p 8080:8080 \
  ghcr.io/avishnuchandra/springboot-s3-pvc-fabric8-poc:latest
```

## Kubernetes / OpenShift Deployment

### Namespace

All resources live in the `springboot-s3-pvc` namespace (or `springboot-s3-pvc-dev` / `springboot-s3-pvc-staging` for non-production environments).

### Prerequisites

1. Provision S3 bucket + IAM with CloudFormation:

```bash
./aws/scripts/create-stack.sh springboot-s3-stack ./aws/examples/dev-stack-params.json
./aws/scripts/setup-bucket-folders.sh <bucket-name>
./aws/scripts/test-s3-setup.sh <bucket-name>
```

2. Create the S3 credentials secret (never commit real credentials):

```bash
# Recommended: imperative creation
kubectl create secret generic s3-credentials \
  --from-literal=AWS_ACCESS_KEY_ID='<your-key-id>' \
  --from-literal=AWS_SECRET_ACCESS_KEY='<your-secret-key>' \
  --from-literal=AWS_ROLE_ARN='<optional-role-arn-for-irsa>' \
  -n springboot-s3-pvc

# Or copy k8s/base/secret.yaml, fill in placeholder values, then:
# kubectl apply -f k8s/base/secret.yaml
```

3. Configure IRSA/STS if you are using role-based auth:
   - Set the ServiceAccount annotation `eks.amazonaws.com/role-arn` in `k8s/base/rbac.yaml`
   - Optionally provide `AWS_ROLE_ARN` in the secret

> **Note**: If your pod uses an IAM role / instance profile (IRSA on EKS, ROSA, etc.), the credentials secret is optional.

### Option A — Apply individual manifests

```bash
# 1. Namespace
kubectl apply -f k8s/base/namespace.yaml

# 2. RBAC
kubectl apply -f k8s/base/rbac.yaml

# 3. ConfigMap & Secret
kubectl apply -f k8s/base/configmap.yaml
# Create the secret imperatively (see above) or apply k8s/base/secret.yaml

# 4. Storage
kubectl apply -f k8s/base/pvc.yaml

# 5. Workload
kubectl apply -f k8s/base/deployment.yaml

# 6. Network
kubectl apply -f k8s/base/service.yaml
kubectl apply -f k8s/base/network-policy.yaml

# 7. OpenShift Route (TLS edge termination)
oc apply -f k8s/base/route.yaml

# 8. Scaling & Availability
kubectl apply -f k8s/base/hpa.yaml
kubectl apply -f k8s/base/pdb.yaml

# 9. Monitoring (requires Prometheus Operator)
kubectl apply -f k8s/base/monitor.yaml
```

### Option B — Deploy with Kustomize

```bash
# Development (LocalStack, minimal resources)
kubectl apply -k k8s/overlays/dev

# Staging
kubectl apply -k k8s/overlays/staging

# Production
kubectl apply -k k8s/overlays/prod
```

Preview what will be applied without actually applying:

```bash
kubectl kustomize k8s/overlays/prod
# or
kustomize build k8s/overlays/prod
```

### Verify the Route

```bash
oc get route springboot-s3-pvc -n springboot-s3-pvc
# NAME                 HOST/PORT                                                        ...
# springboot-s3-pvc   springboot-s3-pvc-springboot-s3-pvc.apps.cluster.example.com   ...
```

### Directory Layout

```
k8s/
├── base/
│   ├── namespace.yaml        # Dedicated namespace
│   ├── secret.yaml           # Secret template (replace placeholders — do not commit real values)
│   ├── configmap.yaml        # Application configuration
│   ├── pvc.yaml              # PersistentVolumeClaim (5 Gi)
│   ├── deployment.yaml       # Deployment with security context, probes, envFrom
│   ├── service.yaml          # ClusterIP Service
│   ├── route.yaml            # OpenShift Route (TLS edge)
│   ├── rbac.yaml             # ServiceAccount, Role, RoleBinding
│   ├── network-policy.yaml   # Ingress/egress network policy
│   ├── hpa.yaml              # HorizontalPodAutoscaler
│   ├── pdb.yaml              # PodDisruptionBudget
│   ├── monitor.yaml          # Prometheus ServiceMonitor
│   └── kustomization.yaml    # Base Kustomization
└── overlays/
    ├── dev/              # LocalStack endpoint, 1 replica, small resources
    ├── staging/          # Real S3 staging bucket, 2 replicas
    └── prod/             # Production bucket, 3+ replicas, full HPA
```

For a full OpenShift checklist, see `OPENSHIFT-DEPLOYMENT.md`.

## AWS S3 CloudFormation

Key assets are under `aws/`:

- `aws/s3-bucket-stack.yaml` - S3 bucket, lifecycle policies, IAM role and policy
- `aws/iam/s3-app-policy.json` - least-privilege folder-scoped policy
- `aws/iam/s3-app-role.yaml` - standalone role template
- `aws/iam/oidc-provider-setup.yaml` - optional OIDC provider for OpenShift service accounts
- `aws/scripts/` - create, update, delete, folder setup, grant permissions, and validation scripts
- `aws/examples/` - dev/staging/prod CloudFormation parameter files

See `aws/AWS-S3-SETUP.md` for the full setup guide and troubleshooting.

## Environment Variables

### S3 Configuration

- `AWS_ACCESS_KEY_ID` - S3 access key (optional for IAM roles)
- `AWS_SECRET_ACCESS_KEY` - S3 secret key (optional for IAM roles)
- `AWS_REGION` - AWS region (default: `us-east-1`)
- `S3_ENDPOINT` - S3 endpoint override (use for LocalStack/MinIO)
- `S3_BUCKET` - S3 bucket name (default: `app-bucket`)
- `S3_INPUT_PREFIX` - Input folder prefix (default: `input/`)
- `S3_PROCESSING_PREFIX` - Processing folder prefix (default: `processing/`)
- `S3_ARCHIVE_PREFIX` - Archive folder prefix (default: `archive/`)
- `S3_ERROR_PREFIX` - Error folder prefix (default: `error/`)
- `S3_COMPLETED_PREFIX` - Completed folder prefix (default: `completed/`)
- `S3_LOGS_PREFIX` - Logs folder prefix (default: `logs/`)
- `AWS_ROLE_ARN` - Optional IAM role ARN for web identity role assumption
- `AWS_WEB_IDENTITY_TOKEN_FILE` - Optional token file path for web identity

### Application Configuration

- `SPRING_PROFILES_ACTIVE` - Active profiles (e.g., `local`, `openshift`)
- `SERVER_PORT` - Server port (default: `8080`)

## Project Structure

```
src/main/java/com/avishnuchandra/s3poc/
├── Application.java              # Spring Boot main class
├── config/
│   └── S3Config.java             # S3 client configuration
├── controller/
│   ├── S3Controller.java         # S3 REST endpoints
│   ├── PvcController.java        # PVC REST endpoints
│   ├── KubernetesController.java # Kubernetes REST endpoints
│   ├── HealthController.java     # Health check endpoint
│   └── FileController.java       # Legacy file endpoints
├── service/
│   ├── S3Service.java            # S3 operations
│   ├── PvcService.java           # PVC file operations
│   └── KubernetesService.java    # Kubernetes operations

k8s/
├── base/
│   ├── namespace.yaml
│   ├── secret.yaml
│   ├── configmap.yaml
│   ├── pvc.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── route.yaml
│   ├── rbac.yaml
│   ├── network-policy.yaml
│   ├── hpa.yaml
│   ├── pdb.yaml
│   ├── monitor.yaml
│   └── kustomization.yaml
└── overlays/
    ├── dev/              # Development environment
    ├── staging/          # Staging environment
    └── prod/             # Production environment

aws/
├── s3-bucket-stack.yaml          # Main CloudFormation template
├── iam/
│   ├── s3-app-policy.json        # IAM policy for app
│   ├── s3-app-role.yaml          # IAM role template
│   └── oidc-provider-setup.yaml  # Optional OIDC provider template
├── scripts/
│   ├── create-stack.sh           # Deploy stack
│   ├── update-stack.sh           # Update stack
│   ├── delete-stack.sh           # Delete stack
│   ├── setup-bucket-folders.sh   # Create folder prefixes
│   ├── grant-permissions.sh      # Attach IAM policy
│   └── test-s3-setup.sh          # Validate S3 setup
├── AWS-S3-SETUP.md               # AWS deployment guide
├── lifecycle-policies-config.json # Lifecycle reference values
└── examples/
    ├── dev-stack-params.json
    ├── staging-stack-params.json
    └── prod-stack-params.json
```

## API Clients

Two REST client collections are included:

- `postman-collection.json` — Postman collection
- `insomnia-collection.json` — Insomnia collection with pre-configured environments:
  - **Local** — `http://localhost:8080`
  - **OpenShift Dev** — `https://springboot-s3-pvc-springboot-s3-pvc-dev.apps.cluster.example.com`
  - **OpenShift Staging** — `https://springboot-s3-pvc-springboot-s3-pvc-staging.apps.cluster.example.com`
  - **OpenShift Prod** — `https://springboot-s3-pvc-springboot-s3-pvc.apps.cluster.example.com`

  Update the environment `base_url` values to match your actual OpenShift cluster hostname.

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) automatically:
- Builds the project on push
- Runs all tests
- Validates Dockerfile

## Testing

The project includes:

- **Unit Tests**: Mock S3 service, controller tests
- **Integration Tests**: Testcontainers + LocalStack for real S3 operations
- **PVC Tests**: Local filesystem operations

Run tests:

```bash
./gradlew test
```

## Troubleshooting

### LocalStack not connecting

```bash
# Verify LocalStack is running
docker ps | grep localstack

# Check logs
docker logs <container_id>

# Restart
docker-compose restart
```

### PVC permission denied

Ensure the pod runs with appropriate permissions and the PVC is mounted:

```bash
kubectl describe pvc springboot-pvc
```

### Kubernetes client not connecting

Verify kubeconfig or ServiceAccount:

```bash
# Check current context
kubectl config current-context

# Verify ServiceAccount in pod
kubectl get sa springboot-s3-pvc
```

## Contributing

See `CONTRIBUTING.md` for guidelines.

## License

MIT
