#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 3 ]]; then
  echo "Usage: $0 <user|role|group> <principal-name> <policy-arn>" >&2
  exit 1
fi

PRINCIPAL_TYPE="$1"
PRINCIPAL_NAME="$2"
POLICY_ARN="$3"

case "${PRINCIPAL_TYPE}" in
  user)
    aws iam attach-user-policy --user-name "${PRINCIPAL_NAME}" --policy-arn "${POLICY_ARN}"
    ;;
  role)
    aws iam attach-role-policy --role-name "${PRINCIPAL_NAME}" --policy-arn "${POLICY_ARN}"
    ;;
  group)
    aws iam attach-group-policy --group-name "${PRINCIPAL_NAME}" --policy-arn "${POLICY_ARN}"
    ;;
  *)
    echo "Unsupported principal type '${PRINCIPAL_TYPE}'. Use user, role, or group." >&2
    exit 1
    ;;
esac

echo "Attached ${POLICY_ARN} to ${PRINCIPAL_TYPE} ${PRINCIPAL_NAME}."
