# AWS S3 CloudFormation Setup Guide

This guide provisions a production-ready S3 bucket for the Spring Boot processing workflow with CloudFormation.

## Prerequisites

- AWS CLI v2 installed and configured (`aws configure`)
- IAM permissions for CloudFormation, S3, and IAM resources
- Bash shell for running helper scripts
- A globally unique S3 bucket name (for example `<project>-<env>-<account>-<region>`)

## Deployed Components

`aws/s3-bucket-stack.yaml` provisions:

- S3 bucket with public access blocked
- Server-side encryption (AES256 or KMS)
- Optional versioning
- Lifecycle management rules for `input/`, `processing/`, `error/`, `completed/`, and `logs/`
- Optional server access logging
- IAM managed policy scoped to application prefixes
- IAM role for EC2/EKS/OpenShift workloads

## Folder Workflow

The logical folders used by the app are:

- `input/` incoming files
- `processing/` in-flight files
- `completed/` successful files
- `error/` failed files
- `archive/` long-term retained files
- `logs/` access and processing log artifacts

Create folder marker objects after stack creation:

```bash
./aws/scripts/setup-bucket-folders.sh <bucket-name>
```

## Deploy CloudFormation Stack

```bash
./aws/scripts/create-stack.sh springboot-s3-stack ./aws/examples/dev-stack-params.json
```

For updates:

```bash
./aws/scripts/update-stack.sh springboot-s3-stack ./aws/examples/dev-stack-params.json
```

For cleanup:

```bash
./aws/scripts/delete-stack.sh springboot-s3-stack
```

## Verify Bucket, Lifecycle, and IAM

```bash
./aws/scripts/test-s3-setup.sh <bucket-name>
```

Optionally set `STACK_NAME` before running test script to verify CloudFormation outputs:

```bash
export STACK_NAME=springboot-s3-stack
./aws/scripts/test-s3-setup.sh <bucket-name>
```

## IAM Setup for Spring Boot Application

### Managed policy and role from stack outputs

```bash
aws cloudformation describe-stacks \
  --stack-name springboot-s3-stack \
  --query "Stacks[0].Outputs[?OutputKey=='S3AppRoleArn' || OutputKey=='S3AppPolicyArn']"
```

### Grant policy to existing user/role/group

```bash
./aws/scripts/grant-permissions.sh role my-openshift-node-role arn:aws:iam::<account-id>:policy/s3-app-policy-dev
```

`aws/iam/s3-app-policy.json` is a reusable policy document template. Replace `${BUCKET_NAME}` with your real bucket name before using it directly with IAM APIs.

### Optional OIDC provider setup for OpenShift service accounts

Use `aws/iam/oidc-provider-setup.yaml` and then configure trust in `aws/iam/s3-app-role.yaml`.

## Lifecycle Policy Behavior

- `input/` transitions to `STANDARD_IA` after `InputTransitionDays`
- `processing/` stale objects are cleaned using expiration and multipart-abort rules
- `error/` objects are expired after `ErrorExpirationDays`
- `completed/` transitions to `GLACIER_IR` after `CompletedToGlacierDays`
- `logs/` objects are expired by retention policy

> Note: S3 lifecycle policies transition object storage classes (for example STANDARD to GLACIER_IR). Moving objects between prefixes/folders (for example `completed/` to `archive/`) must be handled by explicit application logic using copy/delete operations.

## Spring Boot Folder Processing Flow (API Behavior)

Recommended workflow for the existing S3 APIs:

1. On job start, copy from `input/` to `processing/`.
2. On success, copy from `processing/` to `completed/`.
3. On failure, copy from `processing/` to `error/`.
4. Optionally copy curated results from `completed/` to `archive/` (or let lifecycle transition storage class on `completed/`).

Example endpoint usage:

- `POST /s3/upload` with key `input/<file-name>`
- `GET /s3/download/{file-name}` from `processing/` or `completed/`
- `DELETE /s3/delete/{file-name}` for cleanup in `processing/` after copy

## Monitoring and Logging

- Enable `LoggingEnabled=true` and provide `AccessLogBucketName` to ship server access logs
- Use CloudWatch metrics for request count, 4xx/5xx errors, and bucket size
- Enable AWS Cost Explorer tags (`Project`, `Environment`, `ManagedBy`) for chargeback

## Cost Optimization Tips

- Prefer `STANDARD_IA` and `GLACIER_IR` for older objects
- Keep `processing/` cleanup aggressive to avoid stale object costs
- Enable lifecycle expiration for `error/` and `logs/`
- Use KMS only when compliance requires it (extra request cost)

## Troubleshooting

- `BucketAlreadyExists`: choose a new globally unique bucket name
- `AccessDenied` on stack deploy: verify IAM permissions for CloudFormation/IAM/S3
- Logging failure: ensure `AccessLogBucketName` exists and allows S3 log delivery
- OIDC assume-role issues: verify provider thumbprint and `sub` claim mapping
- Missing folder prefixes: run `setup-bucket-folders.sh`
