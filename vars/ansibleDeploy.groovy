def call(Map config = [:]) {

    pipeline {

        agent any

        stages {

            stage('Clone') {
                steps {
                    git url: config.REPO_URL
                }
            }

            stage('User Approval') {

                when {
                    expression {
                        return config.KEEP_APPROVAL_STAGE.toBoolean()
                    }
                }

                steps {
                    input(
                        message: "Deploy to ${config.ENVIRONMENT} ?",
                        ok: "Proceed"
                    )
                }
            }

            stage('Playbook Execution') {

                steps {

                    sh """
                    ansible-playbook ${config.CODE_BASE_PATH}/site.yml
                    """
                }
            }
        }

        post {

            success {

                slackSend(
                    channel: config.SLACK_CHANNEL_NAME,
                    color: 'good',
                    message: "${config.ACTION_MESSAGE} SUCCESS"
                )
            }

            failure {

                slackSend(
                    channel: config.SLACK_CHANNEL_NAME,
                    color: 'danger',
                    message: "${config.ACTION_MESSAGE} FAILED"
                )
            }
        }
    }
}
