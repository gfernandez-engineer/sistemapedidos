pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 45, unit: 'MINUTES')
    }

    environment {
        KUBE_NAMESPACE = 'food-ordering-e2e'
        IMAGE_REGISTRY = 'food-ordering'
        HELM_RELEASE = 'food-ordering'
        HELM_CHART = 'helm/food-ordering-system'
        TRIVY_SEVERITY = 'CRITICAL,HIGH'
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

        stage('Security Scan') {
            steps {
                script {
                    def services = ['users-service', 'orders-service', 'catalog-service', 'payments-service', 'deliveries-service', 'api-gateway']
                    def imageTag = "${env.BUILD_NUMBER}"

                    // 1. OWASP Dependency-Check: scan Maven dependencies for known CVEs
                    echo '=== OWASP Dependency-Check ==='
                    sh 'mvn org.owasp:dependency-check-maven:check -B -DfailBuildOnCVSS=11 -Dformats=HTML,JSON -DoutputDirectory=target/dependency-check || true'

                    // 2. Trivy: scan Docker images for OS and library vulnerabilities
                    echo '=== Trivy Image Scan ==='
                    sh 'mkdir -p target/trivy-reports'
                    services.each { service ->
                        def image = "${IMAGE_REGISTRY}/${service}:${imageTag}"
                        echo "Scanning ${image}..."
                        sh """
                            docker run --rm \
                                -v /var/run/docker.sock:/var/run/docker.sock \
                                -v \$(pwd)/target/trivy-reports:/output \
                                aquasec/trivy image \
                                --severity ${TRIVY_SEVERITY} \
                                --format template \
                                --template '@/contrib/html.tpl' \
                                --output /output/${service}.html \
                                ${image} || true
                        """
                        // Also print summary to console
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image --severity ${TRIVY_SEVERITY} --format table ${image} || true"
                    }
                }
            }
            post {
                always {
                    // Archive OWASP report
                    archiveArtifacts artifacts: 'target/dependency-check/*.html', allowEmptyArchive: true
                    // Archive Trivy reports
                    archiveArtifacts artifacts: 'target/trivy-reports/*.html', allowEmptyArchive: true
                    // Publish HTML reports if HTML Publisher plugin is installed
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/dependency-check',
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'OWASP Dependency Check'
                    ])
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/trivy-reports',
                        reportFiles: 'users-service.html,orders-service.html,catalog-service.html,payments-service.html,deliveries-service.html,api-gateway.html',
                        reportName: 'Trivy Image Scan'
                    ])
                }
            }
        }

        stage('Load Images to K8s') {
            steps {
                script {
                    def services = ['users-service', 'orders-service', 'catalog-service', 'payments-service', 'deliveries-service', 'api-gateway']
                    def imageTag = "${env.BUILD_NUMBER}"
                    def isMinikube = sh(script: 'minikube status 2>/dev/null', returnStatus: true) == 0
                    def isK3s = sh(script: 'which k3s 2>/dev/null', returnStatus: true) == 0

                    if (isMinikube) {
                        echo 'Minikube detected - loading images...'
                        services.each { service ->
                            sh "minikube image load ${IMAGE_REGISTRY}/${service}:latest"
                        }
                    } else if (isK3s) {
                        echo 'K3s detected - importing images to containerd...'
                        services.each { service ->
                            sh "docker save ${IMAGE_REGISTRY}/${service}:${imageTag} ${IMAGE_REGISTRY}/${service}:latest | sudo k3s ctr images import -"
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
                    # Ensure namespace exists with Helm ownership labels (idempotent)
                    kubectl create namespace ${KUBE_NAMESPACE} 2>/dev/null || true
                    kubectl label namespace ${KUBE_NAMESPACE} app.kubernetes.io/managed-by=Helm --overwrite
                    kubectl annotate namespace ${KUBE_NAMESPACE} meta.helm.sh/release-name=${HELM_RELEASE} meta.helm.sh/release-namespace=${KUBE_NAMESPACE} --overwrite

                    # Update Helm dependencies
                    helm dependency update ${HELM_CHART} || true

                    # Deploy using E2E values (nosecurity profile, infra included)
                    helm upgrade --install ${HELM_RELEASE} ${HELM_CHART} \
                        -f ${HELM_CHART}/environments/values-e2e.yaml \
                        --set global.imageTag=${env.BUILD_NUMBER} \
                        --namespace ${KUBE_NAMESPACE} \
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
                    kubectl wait --for=condition=ready pod -l app=users-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app=orders-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app=catalog-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app=payments-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app=deliveries-service -n ${KUBE_NAMESPACE} --timeout=180s
                    kubectl wait --for=condition=ready pod -l app=api-gateway -n ${KUBE_NAMESPACE} --timeout=180s

                    echo 'All pods ready.'
                """
                // Health check via pod readiness status
                sh 'chmod +x scripts/health-check.sh && ./scripts/health-check.sh'
            }
        }

        stage('E2E Tests - Bruno') {
            steps {
                script {
                    // Discover API Gateway NodePort
                    def nodePort = sh(
                        script: "kubectl get svc -n ${KUBE_NAMESPACE} -l app=api-gateway -o jsonpath='{.items[0].spec.ports[0].nodePort}'",
                        returnStdout: true
                    ).trim()

                    // Determine gateway host: Docker container uses host.docker.internal, native Jenkins uses localhost
                    def isDockerized = sh(script: 'test -f /.dockerenv', returnStatus: true) == 0
                    def gatewayHost = isDockerized ? 'host.docker.internal' : 'localhost'
                    def gatewayUrl = "http://${gatewayHost}:${nodePort}"
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
