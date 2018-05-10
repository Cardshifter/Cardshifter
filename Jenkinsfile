#!/usr/bin/env groovy

@Library('ZomisJenkins')
import net.zomis.jenkins.Duga

pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh './gradlew clean distAndTest'
                sh 'mv cardshifter-server/build/libs/cardshifter-server-*.jar cardshifter-server/build/libs/cardshifter-server.jar'
            }
        }

        stage('Docker Image') {
            when {
                branch 'jenkins'
            }
            steps {
                script {
                    // Stop running containers
                    sh 'docker ps -q --filter name="cardshifter_server" | xargs -r docker stop'
                    sh 'echo "missing-security = IGNORE" > server.properties'

                    sh 'docker build . -t cardshifter-server'
                    sh 'docker run -d --rm --name cardshifter_server -v $(pwd):/usr/src/cardshifter -p 192.168.0.110:22737:4242 -p 192.168.0.110:22738:4243 -v /home/zomis/jenkins/cardshifter:/data/logs -w /data/logs cardshifter-server'
                }
            }
        }

/*
                withSonarQubeEnv('My SonarQube Server') {
                    // requires SonarQube Scanner for Maven 3.2+
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar'
                }
*/
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/build/test-results/junit-platform/TEST-*.xml'
        }
        success {
            zpost(0)
        }
        unstable {
            zpost(1)
        }
        failure {
            zpost(2)
        }
    }
}
