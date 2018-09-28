node('java') {

    stage('Provision tools') {
        tool 'Maven 3.5.x'
        tool 'JDK 8'
        tool 'Ant-1.8.2 autoinstall'
    }

    stage('Checkout') {
        checkout scm
    }

    withAnt(installation: 'Ant-1.8.2 autoinstall', jdk: 'JDK 8') {
        withMaven(jdk: 'JDK 8', maven: 'Maven 3.5.x', mavenSettingsConfig: 'd36053eb-3f69-482a-8dcd-d7ea248c53ba') {

            stage('Build') {
                sh "cd mq; mvn -V -U -e clean package"
                archive 'mq/dist/bundles/mq5_1_1.zip'
            }

        }
    }
}
