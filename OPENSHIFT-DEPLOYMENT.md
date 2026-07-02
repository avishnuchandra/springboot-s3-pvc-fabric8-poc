# OpenShift Deployment Guide

## Prerequisites

1. `oc` and `kubectl` CLI installed and authenticated.
2. S3 bucket and IAM resources created from CloudFormation:

```bash
./aws/scripts/create-stack.sh springboot-s3-stack ./aws/examples/dev-stack-params.json
./aws/scripts/setup-bucket-folders.sh <bucket-name>
./aws/scripts/test-s3-setup.sh <bucket-name>
```

3. Update `k8s/secret.yaml` with either IAM user credentials or `role-arn` for IRSA/OIDC mode.

## Deploy

```bash
kubectl apply -f k8s/rbac.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/pvc.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
oc apply -f k8s/route.yaml
```

## Verify

```bash
oc get pods
oc get svc springboot-s3-pvc
oc get route springboot-s3-pvc
curl https://$(oc get route springboot-s3-pvc -o jsonpath='{.spec.host}')/health/all
```
