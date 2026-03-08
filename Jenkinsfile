pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        KUBE_NAMESPACE = 'food-ordering-e2e'
        IMAGE_REGISTRY = 'food-ordering'
        HELM_RELEASE = 'food-ordering'
        HELM_CHART = 'helm/food-ordering-system'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh 'mvn clean verify -B'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['users-service', 'orders-service', 'catalog-service', 'payments-service', 'deliveries-service', 'api-gateway']
                    def imageTag = "${env.BUILD_NUMBER}"

                    // Build images sequentially with pauses to avoid Docker Desktop memory/IO pressure
                    services.eachWithIndex { service, idx ->
                        sh "docker build -f ${service}/Dockerfile -t ${IMAGE_REGISTRY}/${service}:${imageTag} -t ${IMAGE_REGISTRY}/${service}:latest ."
                        if (idx < services.size() - 1) {
                            echo "Pausing 10s to let Docker Desktop release memory..."
                            sleep(time: 10, unit: 'SECONDS')
                        }
                    }
                }
            }
        }

        stage('Load Images to K8s') {
            steps {
                script {
                    def services = ['users-service', 'orders-service', 'catalog-service', 'payments-service', 'deliveries-service', 'api-gateway']
                    def isMinikube = sh(script: 'minikube status 2>/dev/null', returnStatus: true) == 0

                    if (isMinikube) {
                        echo 'Minikube detected - loading images...'
                        services.each { service ->
                            sh "minikube image load ${IMAGE_REGISTRY}/${service}:latest"
                        }
                    } else {
                        echo 'Docker Desktop K8s detected - images already available'
                    }
                }
            }
        }

        stage('Deploy with Helm') {
            steps {
                sh """
                    # Update Helm dependencies
                    helm dependency update ${HELM_CHART} || true

                    # Deploy using E2E values (nosecurity profile, infra included)
                    helm upgrade --install ${HELM_RELEASE} ${HELM_CHART} \
                        -f ${HELM_CHART}/environments/values-e2e.yaml \
                        --set global.imageTag=${env.BUILD_NUMBER} \
                        --namespace ${KUBE_NAMESPACE} \
                        --create-namespace \
                        --wait \
                        --timeout 8m0s
                """
            }
        }

        stage('Wait for Services') {
            steps {
                sh """
                    echo 'Waiting for infrastructure pods...'
                    kubectl wait --for=condition=ready pod -l app=users-db -n ${KUBE_NAMESPACE} --timeout=120s || true
                    kubectl wait --for=condition=ready pod -l app=orders-db -n ${KUBE_NAMESPACE} --timeout=120s || true
                    kubectl wait --for=condition=ready pod -l app=catalog-db -n ${KUBE_NAMESPACE} --timeout=120s || true
                    kubectl wait --for=condition=ready pod -l app=payments-db -n ${KUBE_NAMESPACE} --timeout=120s || true
                    kubectl wait --for=condition=ready pod -l app=deliveries-db -n ${KUBE_NAMESPACE} --timeout=120s || true
                    kubectl wait --for=condition=ready pod -l app=kafka -n ${KUBE_NAMESPACE} --timeout=120s || true

                    echo 'Waiting for microservice pods...'
                    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=users-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=orders-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=catalog-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=payments-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=deliveries-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=api-gateway -n ${KUBE_NAMESPACE} --timeout=180s

                    echo 'All pods ready.'
                """
                // Health check via pod readiness status
                sh 'chmod +x scripts/health-check.sh && ./scripts/health-check.sh'
            }
        }

        stage('E2E Tests - Bruno') {
            steps {
                script {
                    // Discover API Gateway NodePort for E2E access from Jenkins container
                    def nodePort = sh(
                        script: "kubectl get svc -n ${KUBE_NAMESPACE} -l app.kubernetes.io/name=api-gateway -o jsonpath='{.items[0].spec.ports[0].nodePort}'",
                        returnStdout: true
                    ).trim()

                    // From inside Docker container, reach K8s NodePort via host.docker.internal
                    def gatewayUrl = "http://host.docker.internal:${nodePort}"
                    echo "API Gateway URL for E2E: ${gatewayUrl}"

                    // Create dynamic Bruno environment for Jenkins
                    writeFile file: 'bruno-collection/environments/jenkins.bru', text: """vars {
  base_url: ${gatewayUrl}
  users_url: ${gatewayUrl}
  orders_url: ${gatewayUrl}
  catalog_url: ${gatewayUrl}
  payments_url: ${gatewayUrl}
  deliveries_url: ${gatewayUrl}
  user_id: 1
  restaurant_id: 1
  product_id: 1
  order_id: 1
  payment_id: 1
  delivery_id: 1
  driver_id: 1
}
"""

                    sh """
                        cd bruno-collection
                        echo '========================================'
                        echo "Running E2E tests against ${gatewayUrl}"
                        echo '========================================'

                        echo '--- 1. Users ---'
                        bru run --env jenkins 1-users/

                        echo '--- 2. Catalog ---'
                        bru run --env jenkins 2-catalog/

                        echo '--- 3. Orders ---'
                        bru run --env jenkins 3-orders/

                        echo '--- 4. Payments ---'
                        bru run --env jenkins 4-payments/

                        echo '--- 5. Deliveries ---'
                        bru run --env jenkins 5-deliveries/

                        echo '--- 6. Drivers ---'
                        bru run --env jenkins 6-drivers/

                        echo '========================================'
                        echo 'E2E tests completed successfully!'
                        echo '========================================'
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS - Build #${env.BUILD_NUMBER} deployed to K8s (${KUBE_NAMESPACE})"
        }
        failure {
            echo "Pipeline FAILED - Build #${env.BUILD_NUMBER}"
            sh """
                echo '=== Pod Status ==='
                kubectl get pods -n ${KUBE_NAMESPACE} -o wide || true
                echo '=== Recent Events ==='
                kubectl get events -n ${KUBE_NAMESPACE} --sort-by='.lastTimestamp' | tail -30 || true
                echo '=== Failed Pod Logs ==='
                for pod in \$(kubectl get pods -n ${KUBE_NAMESPACE} --field-selector=status.phase!=Running -o jsonpath='{.items[*].metadata.name}' 2>/dev/null); do
                    echo "--- Logs for \$pod ---"
                    kubectl logs \$pod -n ${KUBE_NAMESPACE} --tail=50 || true
                done
            """
        }
        always {
            cleanWs()
        }
    }
}
