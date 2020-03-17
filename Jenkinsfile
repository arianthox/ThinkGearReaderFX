pipeline {
  environment {
      registry = "brainwaves/thinkgear-reader-fx"
      registryCredential = 'dockerhub'
      dockerImage = ''
  }
  agent any
  stages {

    stage('Cloning Git') {
      steps {
        checkout scm
      }
    }

    stage('build_Project'){
       steps{
            sh './gradlew clean build'
       }
    }

  }
}