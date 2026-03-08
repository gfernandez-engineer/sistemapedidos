#!/bin/bash
# Health check script for food-ordering microservices in K8s
# Verifies all services respond via the API Gateway

set -e

NAMESPACE="${KUBE_NAMESPACE:-food-ordering-e2e}"
GATEWAY_URL="${GATEWAY_URL:-http://api.food-ordering.local}"
MAX_RETRIES=30
RETRY_INTERVAL=5

echo "========================================="
echo "Health Check - Food Ordering System"
echo "Namespace: ${NAMESPACE}"
echo "Gateway:   ${GATEWAY_URL}"
echo "========================================="

# Get gateway access URL (NodePort or port-forward)
GATEWAY_PORT=$(kubectl get svc -n ${NAMESPACE} -l app.kubernetes.io/name=api-gateway -o jsonpath='{.items[0].spec.ports[0].nodePort}' 2>/dev/null || echo "")

if [ -n "${GATEWAY_PORT}" ]; then
    GATEWAY_URL="http://localhost:${GATEWAY_PORT}"
    echo "Using NodePort: ${GATEWAY_URL}"
fi

# Function to check service health
check_health() {
    local service=$1
    local url=$2
    local retries=0

    echo -n "Checking ${service}... "
    while [ $retries -lt $MAX_RETRIES ]; do
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${url}" 2>/dev/null || echo "000")
        if [ "$HTTP_CODE" = "200" ]; then
            echo "OK (${HTTP_CODE})"
            return 0
        fi
        retries=$((retries + 1))
        sleep $RETRY_INTERVAL
    done
    echo "FAILED (last code: ${HTTP_CODE})"
    return 1
}

# Check each service health endpoint directly via kubectl port-forward
SERVICES=("users-service:8081" "orders-service:8082" "catalog-service:8083" "payments-service:8084" "deliveries-service:8085" "api-gateway:8080")
ALL_HEALTHY=true

for svc in "${SERVICES[@]}"; do
    NAME="${svc%%:*}"
    PORT="${svc##*:}"

    echo -n "Checking ${NAME}... "
    POD=$(kubectl get pods -n ${NAMESPACE} -l app.kubernetes.io/name=${NAME} -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

    if [ -z "${POD}" ]; then
        echo "NO POD FOUND"
        ALL_HEALTHY=false
        continue
    fi

    # Check pod health via kubectl exec
    HEALTH=$(kubectl exec -n ${NAMESPACE} ${POD} -- curl -s http://localhost:${PORT}/actuator/health 2>/dev/null || echo '{"status":"DOWN"}')
    STATUS=$(echo "${HEALTH}" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)

    if [ "${STATUS}" = "UP" ]; then
        echo "OK (UP)"
    else
        echo "FAILED (${STATUS:-UNKNOWN})"
        ALL_HEALTHY=false
    fi
done

echo "========================================="
if [ "${ALL_HEALTHY}" = true ]; then
    echo "All services are healthy!"
    exit 0
else
    echo "Some services are unhealthy!"
    echo ""
    echo "Pod details:"
    kubectl get pods -n ${NAMESPACE} -o wide
    exit 1
fi
