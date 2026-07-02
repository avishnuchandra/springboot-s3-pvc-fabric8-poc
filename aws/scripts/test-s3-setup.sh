#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <bucket-name> [region]" >&2
  exit 1
fi

BUCKET_NAME="$1"
REGION="${2:-${AWS_REGION:-us-east-1}}"
TMP_UPLOAD_FILE="/tmp/permission-test.txt"
TMP_DOWNLOAD_FILE="/tmp/permission-test-download.txt"

cleanup() {
  rm -f "${TMP_UPLOAD_FILE}" "${TMP_DOWNLOAD_FILE}"
}
trap cleanup EXIT

FOLDERS=(input processing archive error completed logs)

echo "Checking bucket existence..."
aws s3api head-bucket --bucket "${BUCKET_NAME}" --region "${REGION}" >/dev/null

echo "Checking encryption configuration..."
aws s3api get-bucket-encryption --bucket "${BUCKET_NAME}" --region "${REGION}" >/dev/null

echo "Checking versioning status..."
aws s3api get-bucket-versioning --bucket "${BUCKET_NAME}" --region "${REGION}"

echo "Checking lifecycle policies..."
aws s3api get-bucket-lifecycle-configuration --bucket "${BUCKET_NAME}" --region "${REGION}" >/dev/null

echo "Checking folder prefixes..."
for folder in "${FOLDERS[@]}"; do
  count=$(aws s3api list-objects-v2 \
    --bucket "${BUCKET_NAME}" \
    --prefix "${folder}/" \
    --max-keys 1 \
    --query 'KeyCount' \
    --output text \
    --region "${REGION}")

  if [[ "${count}" == "0" ]]; then
    echo "Prefix '${folder}/' missing. Run setup-bucket-folders.sh first." >&2
    exit 1
  fi
  echo "Prefix '${folder}/' verified."
done

echo "Checking IAM role/policy outputs from stack (optional check)..."
if [[ -n "${STACK_NAME:-}" ]]; then
  aws cloudformation describe-stacks --stack-name "${STACK_NAME}" --region "${REGION}" \
    --query 'Stacks[0].Outputs[?OutputKey==`S3AppRoleArn` || OutputKey==`S3AppPolicyArn`]' --output table
fi

echo "Testing S3 permissions (put, get, delete) with current AWS credentials..."
PERMISSION_TEST_KEY="processing/permission-test-$(date +%s).txt"
printf 'permission-test\n' >"${TMP_UPLOAD_FILE}"
aws s3api put-object --bucket "${BUCKET_NAME}" --key "${PERMISSION_TEST_KEY}" --body "${TMP_UPLOAD_FILE}" --region "${REGION}" >/dev/null
aws s3api get-object --bucket "${BUCKET_NAME}" --key "${PERMISSION_TEST_KEY}" "${TMP_DOWNLOAD_FILE}" --region "${REGION}"
aws s3api delete-object --bucket "${BUCKET_NAME}" --key "${PERMISSION_TEST_KEY}" --region "${REGION}"

echo "S3 setup validation passed for ${BUCKET_NAME}."
