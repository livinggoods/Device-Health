pipeline {
    agent none

    environment {
        TEST_PREFIX = "test-IMAGE"
        TEST_IMAGE = "${env.TEST_PREFIX}:${env.BUILD_NUMBER}"
    }

    stages {
        stage('setting up') {
            steps {
                echo "setting up build"
            }
        }


        stage('Build Projects') {
            parallel{
                stage('server') { 
                    agent {
                        docker {
                            image 'maven:alpine' 
                            args '-v $HOME/.m2:/root/.m2'
                        }
                    }
                    steps {
                        def targetVersion = getDevVersion()
                        print 'target build version...'
                        print targetVersion
                        //mvn -B -DskipTests clean package
                        sh "mvn -Dintegration-tests.skip=true -Dbuild.number=${targetVersion} clean package"
                        def pom = readMavenPom file: 'pom.xml'
                        // get the current development version
                        developmentArtifactVersion = "${pom.version}-${targetVersion}"
                        print pom.version

                        sh '''
                            cd server
                            mvn -B -DskipTests clean package
                        '''
                    
                    }
                     // Run integration test
                    steps {
                        // just to trigger the integration test without unit testing
                        sh "mvn  verify -Dunit-tests.skip=true"
                    }
              }
        
                }
                stage('frontend') { 
                    agent {
                        docker {
                            image 'node:10.8.0-alpine' 
                            args '-u 0:0'
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
        stage("Build and run images") {
            steps {
                sh "docker-composer build"
                sh "docker-compose up -d"
                
            }
        }

        stage("Run tests") {
            steps {
                sh "docker-compose exec -T php-fpm composer --no-ansi --no-interaction tests-ci"
                sh "docker-compose exec -T php-fpm composer --no-ansi --no-interaction behat-ci"
            }
        }
        stage('Push to Docker Registry'){
         withCredentials([usernamePassword(credentialsId: 'dockerHubAccount', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
         pushToImage(CONTAINER_NAME, CONTAINER_TAG, USERNAME, PASSWORD)
         sh "docker login -u=$REGISTRY_AUTH_USR -p=$REGISTRY_AUTH_PSW ${env.REGISTRY_ADDRESS}"
                sh "docker-compose -f ${env.COMPOSE_FILE} build"
                sh "docker-compose -f ${env.COMPOSE_FILE} push"

        }
        

}     


    }

    def getDevVersion() {
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber;
    if (gitCommit == null) {
        versionNumber = env.BUILD_NUMBER;
    } else {
        versionNumber = gitCommit.take(8);
    }
    print 'build  versions...'
    print versionNumber
    return versionNumber
}

def getReleaseVersion() {
    def pom = readMavenPom file: 'server/pom.xml'
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber;
    if (gitCommit == null) {
        versionNumber = env.BUILD_NUMBER;
    } else {
        versionNumber = gitCommit.take(8);
    }
    return pom.version.replace("-SNAPSHOT", ".${versionNumber}")
}


}