node('nimble-jenkins-slave') {

    // -----------------------------------------------
    // --------------- Staging Branch ----------------
    // -----------------------------------------------
    if (env.BRANCH_NAME == 'staging') {

        stage('Clone and Update') {
            git(url: 'https://github.com/nimble-platform/catalog-service.git', branch: env.BRANCH_NAME)
        }

        stage('Build Dependencies') {
            sh 'rm -rf common'
            sh 'git clone https://github.com/nimble-platform/common'
            dir('common') {
                sh 'git checkout ' + env.BRANCH_NAME
                sh 'mvn clean install'
            }
        }

        stage('Build Java') {
            sh '/bin/bash -xe deploy.sh java-build'
        }

        stage('Build Docker') {
            sh '/bin/bash -xe deploy.sh docker-build-staging'
        }

        stage('Push Docker') {
            sh 'docker push nimbleplatform/catalogue-service-micro:staging'
        }

        stage('Deploy') {
            sh 'ssh staging "cd /srv/nimble-staging/ && ./run-staging.sh restart-single catalog-service-srdc"'
        }
    }

    // -----------------------------------------------
    // ---------------- Master Branch ----------------
    // -----------------------------------------------
    if (env.BRANCH_NAME == 'master') {
        stage('Clone and Update') {
            git(url: 'https://github.com/nimble-platform/catalog-service.git', branch: env.BRANCH_NAME)
        }

        stage('Build Dependencies') {
            sh 'rm -rf common'
            sh 'git clone https://github.com/nimble-platform/common'
            dir('common') {
                sh 'git checkout ' + env.BRANCH_NAME
                sh 'mvn clean install'
            }
        }

        stage('Build Java') {
            sh '/bin/bash -xe deploy.sh java-build'
        }
    }

    // -----------------------------------------------
    // ---------------- Release Tags -----------------
    // -----------------------------------------------
    if( env.TAG_NAME ==~ /^\d+.\d+.\d+$/) {

        stage('Clone and Update') {
            git(url: 'https://github.com/nimble-platform/catalog-service.git', branch: 'master')
        }

        stage('Build Dependencies') {
            sh 'rm -rf common'
            sh 'git clone https://github.com/nimble-platform/common'
            dir('common') {
                sh 'git checkout master'
                sh 'mvn clean install'
            }
        }

        stage('Set version') {
            sh 'mvn versions:set -DnewVersion=' + env.TAG_NAME
            sh 'mvn -f catalogue-service-micro/pom.xml versions:set -DnewVersion=' + env.TAG_NAME
        }

        stage('Build Java') {
            sh '/bin/bash -xe deploy.sh java-build'
        }

        stage('Build Docker') {
            sh '/bin/bash -xe deploy.sh docker-build'
        }

        stage('Push Docker') {
            sh 'mvn -f catalogue-service-micro/pom.xml docker:build docker:push -P docker'
            sh 'mvn -f catalogue-service-micro/pom.xml docker:build docker:push -P docker -Ddocker.image.tag=' + env.TAG_NAME
        }

        stage('Deploy MVP') {
            sh 'ssh nimble "cd /data/deployment_setup/prod/ && sudo ./run-prod.sh restart-single catalog-service-srdc"'
        }

        stage('Deploy FMP') {
            sh 'ssh fmp-prod "cd /srv/nimble-fmp/ && ./run-fmp-prod.sh restart-single catalogue-service"'
        }
    }
}
