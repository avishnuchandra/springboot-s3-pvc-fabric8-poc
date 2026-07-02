#!/usr/bin/env bash
set -euo pipefail

BUCKET_NAME="${1:-}"
REGION="${AWS_REGION:-us-east-1}"

if [[ -z "${BUCKET_NAME}" ]]; then
  echo "Usage: $0 <bucket-name>" >&2
  exit 1
fi

FOLDERS=(input processing archive error completed logs)

for folder in "${FOLDERS[@]}"; do
  echo "Ensuring prefix '${folder}/' exists"
  aws s3api put-object \
    --bucket "${BUCKET_NAME}" \
    --key "${folder}/" \
    --region "${REGION}" \
    --body /dev/null >/dev/null
done

echo "Folder setup complete for s3://${BUCKET_NAME}/"
