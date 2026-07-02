#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AWS_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

STACK_NAME="${1:-springboot-s3-stack}"
PARAM_FILE="${2:-${AWS_DIR}/examples/dev-stack-params.json}"
REGION="${AWS_REGION:-us-east-1}"

if [[ ! -f "${PARAM_FILE}" ]]; then
  echo "Parameter file not found: ${PARAM_FILE}" >&2
  exit 1
fi

echo "Creating stack '${STACK_NAME}' in region '${REGION}' using parameters '${PARAM_FILE}'"
aws cloudformation create-stack \
  --stack-name "${STACK_NAME}" \
  --template-body "file://${AWS_DIR}/s3-bucket-stack.yaml" \
  --parameters "file://${PARAM_FILE}" \
  --capabilities CAPABILITY_NAMED_IAM \
  --region "${REGION}"

aws cloudformation wait stack-create-complete --stack-name "${STACK_NAME}" --region "${REGION}"

echo "Stack created successfully. Outputs:"
aws cloudformation describe-stacks \
  --stack-name "${STACK_NAME}" \
  --region "${REGION}" \
  --query 'Stacks[0].Outputs'
