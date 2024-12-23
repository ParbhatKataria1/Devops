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
                    sh 'nvm install 20.11.0'
                    sh 'nvm use 20.11.0'
                    sh 'npm install'
                    sh 'npm test'
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