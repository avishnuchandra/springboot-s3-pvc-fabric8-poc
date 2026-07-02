# OpenShift Deployment Guide

## Prerequisites

1. `oc` and `kubectl` CLI installed and authenticated.
2. S3 bucket and IAM resources created from CloudFormation:

```bash
./aws/scripts/create-stack.sh springboot-s3-stack ./aws/examples/dev-stack-params.json
./aws/scripts/setup-bucket-folders.sh <bucket-name>
./aws/scripts/test-s3-setup.sh <bucket-name>
```

3. Create/update `k8s/base/secret.yaml` with IAM user credentials or `AWS_ROLE_ARN` for IRSA/OIDC mode.

## Deploy

```bash
# Recommended: apply the OpenShift production overlay
kubectl apply -k k8s/overlays/prod

# Or apply base manifests individually
kubectl apply -f k8s/base/rbac.yaml
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secret.yaml
kubectl apply -f k8s/base/pvc.yaml
kubectl apply -f k8s/base/deployment.yaml
kubectl apply -f k8s/base/service.yaml
oc apply -f k8s/base/route.yaml
```

## Verify

```bash
oc get pods
oc get svc springboot-s3-pvc
oc get route springboot-s3-pvc
curl https://$(oc get route springboot-s3-pvc -o jsonpath='{.spec.host}')/health/all
```
