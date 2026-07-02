# TASKS for springboot-s3-pvc-fabric8-poc

Repository: avishnuchandra/springboot-s3-pvc-fabric8-poc
Created by: GitHub Copilot Chat Assistant

Use this file to track short-term actionable tasks and priorities for the PoC.

## 1. Project metadata and documentation
- [ ] Update README.md with clear build & run instructions (maven/gradle, Docker, kubectl/fabric8).
- [ ] Add CONTRIBUTING.md with branching and PR guidelines.
- [ ] Keep docs/architecture.md in sync with repo details.

## 2. Build & CI
- [ ] Confirm build tool (pom.xml or build.gradle) and ensure CI runs the correct commands.
- [ ] Add GitHub Actions workflow for build, test, and static analysis.
- [ ] Add a reproducible Docker image build step in CI and push to a registry (or GitHub Container Registry).

## 3. Testing
- [ ] Add unit tests for core services (S3 client wrapper, controllers).
- [ ] Add integration tests that run against MinIO or LocalStack in CI.
- [ ] Ensure tests run fast and fail CI on regressions.

## 4. Containerization & Kubernetes
- [ ] Verify or add Dockerfile with a pinned Java base image and multi-stage build.
- [ ] Add Kubernetes manifests (Deployment, Service, PVC, ConfigMap, Secret) or a Helm chart.
- [ ] Add readiness/liveness probes and resource requests/limits.

## 5. S3 & storage
- [ ] Centralize S3 configuration with environment variables and application.yml.
- [ ] Document expected env vars (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION, S3_BUCKET).
- [ ] Add example local dev setup using MinIO (docker-compose or k8s manifests).

## 6. Observability & security
- [ ] Expose Prometheus metrics via Micrometer and add a sample dashboard.
- [ ] Add structured logging and correlation IDs for requests.
- [ ] Ensure secrets are only stored in Kubernetes Secrets or CI secrets.

## 7. Quality & maintenance
- [ ] Add static analysis and code style checks (checkstyle/spotless) to CI.
- [ ] Pin dependencies and base images where practical.
- [ ] Add periodic dependency-scan (Dependabot or similar).

## 8. Optional improvements
- [ ] Add OpenTelemetry tracing integration.
- [ ] Add example Helm chart and deployment guide.
- [ ] Provide sample Postman collection or curl examples for API endpoints.

---

Notes
- If you want, I can create a branch and open a PR to implement any subset of these tasks (for example: add CI workflow, or add Dockerfile improvements). Tell me which tasks you want me to work on first.
