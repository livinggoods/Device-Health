pipeline {
    agent none
    stages {
        stage('Build Project') {
            parallel{
                stage('server') { 
                    agent {
                        docker {
                            image 'maven:alpine' 
                            args '-v $HOME/.m2:/root/.m2'
                        }
                    }
                    steps {
                        sh '''
                            cd server
                            mvn -B -DskipTests clean package
                        '''
                        sh ''
                    }
                }
                stage('frontend') { 
                    agent {
                        docker {
                            image 'node:alpine' 
                            args '-u 0:0'
                        }
                    }
                    steps {
                        sh '''
                            cd frontend
                            npm i npm@latest -g
                            npm run build
                        '''
                    }
                }
            }
        }
    }
}