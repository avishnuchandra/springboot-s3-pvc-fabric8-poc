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

UPDATE_OUTPUT=$(aws cloudformation update-stack \
  --stack-name "${STACK_NAME}" \
  --template-body "file://${AWS_DIR}/s3-bucket-stack.yaml" \
  --parameters "file://${PARAM_FILE}" \
  --capabilities CAPABILITY_NAMED_IAM \
  --region "${REGION}" 2>&1) || STATUS=$?
STATUS="${STATUS:-0}"

if [[ ${STATUS} -ne 0 ]]; then
  if grep -q "No updates are to be performed" <<< "${UPDATE_OUTPUT}"; then
    echo "No updates were required for stack '${STACK_NAME}'."
    exit 0
  fi
  echo "Failed to update stack '${STACK_NAME}':" >&2
  echo "${UPDATE_OUTPUT}" >&2
  exit ${STATUS}
fi

echo "Waiting for stack update to complete..."
aws cloudformation wait stack-update-complete --stack-name "${STACK_NAME}" --region "${REGION}"
echo "Stack update complete."
