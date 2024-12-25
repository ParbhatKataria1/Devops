pipeline {
    agent any

    environment {
        NEXT_PUBLIC_SUCCESS_DIV = credentials('NEXT_PUBLIC_SUCCESS_DIV')
        HEROKU_API_KEY = credentials('HEROKU_TOKEN')
        HEROKU_EMAIL = credentials('HEROKU_USERNAME')
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
                    sh 'npm run build' // building build here to have better control over environemnt.
                }
            }
        }

        stage('deployment to heroku'){
            when{
                expression {currentBuild.result == null || currentBuild.result == 'SUCCESS'} // checking previous stages were successful
            }

            steps{
                sh '''
                # Check if Heroku CLI is installed and executable; install it if not
                    if ! [ -x "$(command -v heroku)" ] ; then
                        curl https://cli-assets.heroku.com/install-ubuntu.sh | sh
                    else
                        echo "Heroku is already installed"
                    fi
                echo "machine api.heroku.com
                    login $HEROKU_EMAIL
                    password $HEROKU_PASSWORD
                " > ~/.netrc
                chmod 600 ~/.netrc


                HEROKU_APP_NAME=HEROKU_APP_$(date +%s)

                heroku create $HEROKU_APP_NAME
                heroku config:set NEXT_PUBLIC_SUCCESS_DIV=$NEXT_PUBLIC_SUCCESS_DIV --app $HEROKU_APP_NAME
                
                # Add Heroku Git remote
                git remote add heroku https://git.heroku.com/$HEROKU_APP_NAME.git || true

                # Push built files to Heroku
                git add .next public package.json

                # Commit changes, skip if no new changes exist
                git commit -m 'push:commit from jenkins'|| true
                git push heroku main 

                '''


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
