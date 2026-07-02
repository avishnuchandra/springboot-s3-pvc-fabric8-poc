#!/usr/bin/env bash
set -euo pipefail

STACK_NAME="${1:-springboot-s3-stack}"
REGION="${AWS_REGION:-us-east-1}"

echo "Deleting stack '${STACK_NAME}' in region '${REGION}'"
aws cloudformation delete-stack --stack-name "${STACK_NAME}" --region "${REGION}"
aws cloudformation wait stack-delete-complete --stack-name "${STACK_NAME}" --region "${REGION}"

echo "Stack '${STACK_NAME}' deleted."
