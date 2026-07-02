# GitHub Copilot instructions for this repository

Repository: springboot-s3-pvc-fabric8-poc
Owner: avishnuchandra

Purpose
- This repository is a proof-of-concept Spring Boot application demonstrating integration with S3 and persistent volume claims (PVC) using Fabric8 (Kubernetes-related tooling). Use the repository files to discover exact build and run instructions — do not assume a particular build tool without checking the repository (pom.xml or build.gradle).

What to do when asked to change code
- Inspect the repository structure and locate build files (pom.xml, build.gradle), Dockerfile(s), and README first.
- Run or suggest the correct build command based on the detected build tool:
  - Maven: `mvn clean package` (tests: `mvn test`)
  - Gradle: `./gradlew build` (tests: `./gradlew test`)
- When proposing code changes, prefer small, well-documented commits that include:
  - Implementation change
  - Unit tests covering the change when feasible
  - Any necessary configuration updates or migration notes
- If making changes that affect runtime configuration (S3 credentials, region, Kubernetes manifests), do NOT hardcode secrets. Use environment variables, externalized configuration (application.properties / application.yml), or Kubernetes Secrets.

Testing and CI
- Run the test suite locally before suggesting changes. If tests are slow, run the unit tests first.
- If CI is configured (GitHub Actions), check workflow files in `.github/workflows` and use the same commands used by CI.
- When a suggested change might break CI, include instructions to update workflow files and explain why.

Containers, Kubernetes, and Fabric8
- Look for Dockerfiles and Maven/Gradle plugins (fabric8-maven-plugin). When suggesting container-related changes, prefer reproducible builds and pinned base images where appropriate.
- For Kubernetes manifests or Fabric8 resources, prefer declarative, idempotent changes. Document the expected environment (namespaces, PVC sizes, access modes) in the PR description.

Code style and quality
- Follow the existing project's style. If there are code style checks (checkstyle, spotless, formatter), run them and fix violations.
- Prefer clear, idiomatic Java and Spring Boot patterns. Keep public APIs stable unless a breaking change is intended and documented.

Security and secrets
- Never add credentials, secret keys, or private tokens to the repo. If a change requires secrets for testing, show example environment variable names and mention how to configure them in GitHub Actions using repository secrets.

Documentation
- Keep README and in-repo docs up-to-date for any changes that affect setup, configuration, or running the app.
- When adding features, include example commands to build, run, and test locally, plus example Kubernetes/app manifests if relevant.

Pull requests and communication
- Use feature/<short-desc> branch names for new work. Use clear commit messages and a descriptive PR body that lists: what changed, why it changed, how to test, and any deployment/rollback notes.
- If uncertain about intent or missing context, ask the repo owner (@avishnuchandra) before making large changes.

If you cannot find required information
- Look for README.md, CONTRIBUTING.md, and files in `.github/` for conventions.
- If the repository lacks instructions for running or testing, propose a minimal README update with steps derived from the build files and Docker/Kubernetes manifests.

Contact
- Repo owner/maintainer: @avishnuchandra

Assistant-specific guidance
- When creating or editing files, include concise explanations in commit messages and PR descriptions.
- Provide code diffs and rationale in the response. If tests are not present for a change, suggest test cases and how to automate them.
- Prioritize correctness, testability, and security. When in doubt, ask for clarification from the user/maintainer.
