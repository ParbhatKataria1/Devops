pipeline{
    agent any

    environment {
        NEXT_PUBLIC_SUCCESS_DIV=credentials('NEXT_PUBLIC_SUCCESS_DIV')
    }

    stages{
        stage('checkout scm'){
            steps{
                git url: 'https://github.com/ParbhatKataria1/Devops', branch :'main'
            }
        }


        stage('run tests'){
            steps{
                dir('devlops/next_project'){
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
    }

    post{
        success {
            echo "Pipeline completed successfully"
        }
        failure {
            echo "Pipeline failed"
        }
    }
}