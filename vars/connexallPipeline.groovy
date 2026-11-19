/**
 * Pipeline CI/CD generalizada da ConnexALL.
 */
def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            JAVA_HOME = 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.13.9-hotspot'
            ANDROID_SDK_ROOT = 'C:\\Users\\jrego\\AppData\\Local\\Android\\Sdk'
            TMP = "${WORKSPACE}\\tmp"
            TEMP = "${WORKSPACE}\\tmp"
        }

        stages {
            stage('Checkout') {
                steps {
                    bat 'if not exist "%TMP%" mkdir "%TMP%"'
                    checkout scm
                }
            }

            stage('Install') { steps { bat 'make install' } }
            stage('Lint') { steps { bat 'make lint' } }
            stage('Type-check') { steps { bat 'make type-check' } }
            stage('Unit Tests') { steps { bat 'make test' } }
            stage('E2E Tests') { steps { bat 'make test-e2e' } }
            stage('Accessibility') { steps { bat 'make a11y' } }

            // 1. VALIDAR O CÓDIGO PRIMEIRO
            stage('SonarQube') {
                steps {
                    bat """
                        set /p SONAR_TOKEN=<C:\\Users\\jrego\\sonar-token.txt
                        C:\\SonarScanner-8.1.0\\sonar-scanner-8.1.0.6389-windows-x64\\bin\\sonar-scanner.bat -Dsonar.token=%SONAR_TOKEN% -Dsonar.host.url=http://127.0.0.1:9000 -Dsonar.qualitygate.wait=true
                    """
                }
            }

            // 2. COMPILAR OS ARTEFACTOS NATIVOS
            stage('Build') {
                steps {
                    bat 'make build'
                }
            }

            // 3. CONSTRUIR AS IMAGENS DOCKER (WRAPPING)
            stage('Docker Build') {
                steps {
                    bat 'make docker-build'
                }
            }

            // 4. LEVANTAR CONTAINERS E TESTAR COMUNICAÇÃO (Sem rebuild)
            stage('Container Integration Test') {
                steps {
                    bat 'make test-integration'
                }
            }

            stage('Audit Report') {
                steps {
                    bat '"C:/Users/jrego/AppData/Local/Python/pythoncore-3.14-64/python.exe" scripts/generate_report.py'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'reports',
                        reportFiles: 'report.html',
                        reportName: 'Audit Report (ISO/FDA)'
                    ])
                }
            }
        }

        post {
            success {
                archiveArtifacts artifacts: 'dist/**, bin/**, app/build/outputs/**, reports/**', fingerprint: true
                echo 'Build passed'
            }
            failure {
                echo 'Build failed - check logs'
            }
        }
    }
}