pipeline {
    agent none
    stages {
        stage('Build Backend') { 
            agent {
                docker {
                    image 'maven:3-alpine' 
                    args '-v /root/.m2:/root/.m2' 
                }
            }
            steps {
                sh 'cd backend'
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Build Frontend') { 
            agent {
                docker {
                    image 'node:8-alpine' 
                }
            }
            steps {
                sh 'cd frontend'
                sh 'npm i'
                sh 'npm run build'
            }
        }
    }
}