pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Common Module') {
            steps {
                dir('common') {
                    sh 'mvn clean install -DskipTests -B'
                }
            }
        }

        stage('Build & Test Services') {
            parallel {
                stage('Users Service') {
                    steps {
                        dir('users-service') {
                            sh 'mvn clean verify -B'
                        }
                    }
                }
                stage('Orders Service') {
                    steps {
                        dir('orders-service') {
                            sh 'mvn clean verify -B'
                        }
                    }
                }
                stage('Catalog Service') {
                    steps {
                        dir('catalog-service') {
                            sh 'mvn clean verify -B'
                        }
                    }
                }
                stage('Payments Service') {
                    steps {
                        dir('payments-service') {
                            sh 'mvn clean verify -B'
                        }
                    }
                }
                stage('Deliveries Service') {
                    steps {
                        dir('deliveries-service') {
                            sh 'mvn clean verify -B'
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn clean verify -B'
                        }
                    }
                }
            }
        }

        stage('Test Reports') {
            steps {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['users-service', 'orders-service', 'catalog-service', 'payments-service', 'deliveries-service', 'api-gateway']
                    def imageTag = "${env.BUILD_NUMBER}"

                    services.each { service ->
                        dir(service) {
                            sh "docker build -t food-ordering/${service}:${imageTag} -t food-ordering/${service}:latest ."
                        }
                    }
                }
            }
        }

        stage('Deploy with Docker Compose') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS - Build #${env.BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline FAILED - Build #${env.BUILD_NUMBER}"
        }
        always {
            cleanWs()
        }
    }
}
