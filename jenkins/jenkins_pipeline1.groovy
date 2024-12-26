pipeline {
    agent any

    environment {
        NEXT_PUBLIC_SUCCESS_DIV = credentials('NEXT_PUBLIC_SUCCESS_DIV')
        HEROKU_API_KEY = credentials('HEROKU_TOKEN')
        HEROKU_EMAIL = credentials('HEROKU_USERNAME')
        EC2_SECRET_KEY = credentials('EC2_SECRET_KEY')
        EC2_INSTANCE = credentials('EC2_INSTANCE')
    }

    stages {
        stage('checkout scm') {
            steps {
                git url: 'https://github.com/ParbhatKataria1/Devops', branch :'main'
            }
        }

        stage('run tests') {
            steps {
                dir('next_project') {
                    sh '''
                    export NVM_DIR="$HOME/.nvm"
                    [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
                    nvm install 20.11.0
                    nvm use 20.11.0
                    node --version
                    npm install
                    npm test
                    '''
                }
            }
        }

        stage('build application') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }  // checking previous stages were successful
            }
            steps {
                dir('next_project') {
                    sh '''
                    export NVM_DIR="$HOME/.nvm"
                    [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
                    nvm use 20.11.0
                    node --version
                    #  building build here to have better control over environemnt.
                    npm run build
                    '''
                }
            }
        }

        stage('deployment to ec2 instance') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' } // checking previous stages were successful
            }

            steps {
                sshagent(['EC2_SECRET_KEY']) {
                    sh '''

                     scp -o StrictHostKeyChecking=no package.json .next public $EC2_INSTANCE:~/next_project

                     ssh $EC2_INSTANCE<<EOF
                     cd ~/next_project
                     npm install -g pm2
                     pm2 start -- name "next_project" -- start
                     pm2 save
                     EOF

                '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
}
