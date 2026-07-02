# Architecture

This document describes the high-level architecture of the springboot-s3-pvc-fabric8-poc repository — a proof-of-concept Spring Boot application demonstrating S3 integration and use of persistent volume claims (PVC) with Fabric8/Kubernetes.

Overview
- Application: Spring Boot (Java) web service exposing REST endpoints for file upload/download and basic metadata operations.
- Object storage: Amazon S3 (or S3-compatible service) for storing application file objects.
- Persistent storage: Kubernetes PersistentVolume + PersistentVolumeClaim used for local filesystem persistence inside pods when required (e.g., temporary processing, caches, or local artifacts).
- Containerization: Docker images produced from the application build. The repository may use a Dockerfile and/or build plugins to create container images.
- Kubernetes: Manifests or Fabric8 Maven/Gradle plugin resources to generate/deploy Kubernetes Deployment, Service, PVC, and ConfigMap/Secret objects.

Core components
1. Spring Boot application
   - Handles incoming HTTP requests, performs business logic, and orchestrates uploads/downloads between local disk (when used) and S3.
   - Configuration is externalized in application.properties / application.yml and supports environment-variable overrides.
   - Credentials and sensitive values are injected from environment variables or Kubernetes Secrets — do not hardcode.

2. S3 Integration
   - The app uses an S3 client (AWS SDK or compatible library) to PUT/GET objects to a bucket.
   - Typical configuration items: S3 endpoint (for compatibility layers), region, bucket name, access key ID, secret access key, and optional session token.
   - For local development, recommend using MinIO or LocalStack to emulate S3.

3. Persistent Volume Claim (PVC)
   - A PVC is mounted into the pod to provide persistent filesystem storage.
   - Use PVC for workload needs such as temporary object staging, processing, or when relying on a filesystem for library compatibility.
   - PVC size and access mode should be declared in Kubernetes manifests and documented in deployment notes.

4. Container / Kubernetes deployment
   - Dockerfile builds the application image. Prefer reproducible, minimal base images (e.g., Eclipse Temurin, Distroless) and pin versions.
   - Kubernetes resources include: Deployment, Service, ConfigMap, Secret, PersistentVolumeClaim, and optional Ingress or ServiceMonitor.
   - Use readiness and liveness probes to ensure reliable pod lifecycle management.

Configuration and secrets
- Use environment variables or application configuration files for non-sensitive settings.
- Store AWS/S3 credentials in Kubernetes Secrets and mount them as environment variables or files. Example env var names: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION, S3_ENDPOINT, S3_BUCKET.
- For local development, prefer a .env file (not checked into source) or local profiles to inject test credentials for MinIO/LocalStack.

Data flow (high-level)
1. Client sends file upload request to Spring Boot REST endpoint.
2. Application validates request and optionally writes the incoming stream to a local temp file mounted on the PVC.
3. Application uploads the file to S3 (PUT), storing metadata (key, size, content-type) in internal storage or returning it to client.
4. For download, application fetches the object from S3 (GET) and streams it back to the client. Optionally cache to PVC for repeated reads.

Observability and reliability
- Logging: Use structured logs (JSON where appropriate) and include correlation IDs for request tracing.
- Metrics: Expose Prometheus metrics endpoint (micrometer) to monitor request rates, success/failure counts, upload/download latency, and S3 client errors.
- Tracing: Integrate distributed tracing (OpenTelemetry/Zipkin) if the app will be part of a larger system.
- Health checks: Implement liveness/readiness endpoints and verify S3 connectivity as part of readiness if the service cannot operate without it.

Security considerations
- Never commit credentials into the repository. Use secrets management for production credentials.
- Ensure least privilege for S3 credentials (narrow IAM policies to required buckets and actions).
- Validate and sanitize file metadata (e.g., file names, content types) to avoid injection or path traversal risks when using local filesystem staging.
- Limit maximum upload size and apply rate limiting to prevent abuse.

Testing and local development
- Use unit tests for business logic and integration tests that can run against MinIO or LocalStack.
- Provide a local docker-compose or Kubernetes manifest for local testing that includes a local S3 emulator (MinIO) and mounts for PVC emulation.
- When running in CI, use ephemeral test buckets and rotate credentials. Clean up test artifacts after the run.

Recommendations and next steps
- Add a deployment README with exact build and deploy commands discovered from project files (mvn/gradle commands, Docker build/push, and kubectl or fabric8 plugin usage).
- Add example Kubernetes manifests or Helm chart for easier adoption.
- Include Prometheus metrics and sample Grafana dashboard templates for common S3/file metrics.
- Add automated integration tests that exercise S3 upload/download using a local emulator in CI.

Appendix: Example environment variables
- SPRING_PROFILES_ACTIVE=dev
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- AWS_REGION
- S3_ENDPOINT (for MinIO or other S3-compatible services)
- S3_BUCKET

If you want, I can:
- Create this file in the repository now.
- Tailor the document to details discovered in the repo (e.g., exact build tool, Dockerfile path, and existing manifests).
Which would you prefer?