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
                sh '''
                    ls -a
                    cd backend
                    mvn -B -DskipTests clean package
                '''
                sh ''
            }
        }
        stage('Build Frontend') { 
            agent {
                docker {
                    image 'node:8-alpine' 
                }
            }
            steps {
                sh '''
                    cd frontend
                    npm i
                    npm run build
                '''
            }
        }
    }
}