#!/bin/bash
# Health check script for food-ordering microservices in K8s
# Verifies all services are ready via kubectl pod readiness

set -e

NAMESPACE="${KUBE_NAMESPACE:-food-ordering-e2e}"
MAX_RETRIES=30
RETRY_INTERVAL=5

echo "========================================="
echo "Health Check - Food Ordering System"
echo "Namespace: ${NAMESPACE}"
echo "========================================="

SERVICES=("users-service" "orders-service" "catalog-service" "payments-service" "deliveries-service" "api-gateway")
ALL_HEALTHY=true

for NAME in "${SERVICES[@]}"; do
    echo -n "Checking ${NAME}... "
    POD=$(kubectl get pods -n ${NAMESPACE} -l app=${NAME} -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

    if [ -z "${POD}" ]; then
        echo "NO POD FOUND"
        ALL_HEALTHY=false
        continue
    fi

    # Check pod readiness condition (set by K8s readinessProbe httpGet /actuator/health)
    READY=$(kubectl get pod ${POD} -n ${NAMESPACE} -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null || echo "False")

    if [ "${READY}" = "True" ]; then
        echo "OK (Ready)"
    else
        # Retry with wait
        RETRIES=0
        while [ $RETRIES -lt $MAX_RETRIES ]; do
            sleep $RETRY_INTERVAL
            READY=$(kubectl get pod ${POD} -n ${NAMESPACE} -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null || echo "False")
            if [ "${READY}" = "True" ]; then
                echo "OK (Ready after retry)"
                break
            fi
            RETRIES=$((RETRIES + 1))
        done
        if [ "${READY}" != "True" ]; then
            echo "FAILED (Not Ready)"
            ALL_HEALTHY=false
        fi
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
    echo ""
    echo "Pod events:"
    kubectl describe pods -n ${NAMESPACE} --field-selector=status.phase!=Running 2>/dev/null | grep -A5 "Events:" || true
    exit 1
fi
