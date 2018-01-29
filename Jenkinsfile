#!/usr/bin/env groovy

def projectProperties = [
  buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '1000')),
  [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
]

properties(projectProperties)

timestamps {
  node('master') {
    try {
      stage('Checkout') {
        checkout scm
      }

      stage('Tests') {
        //stuff that could possibly throw exceptions like failed tests
        currentBuild.result = 'SUCCESS'
      }
    } catch (err) {
      currentBuild.result = 'FAILURE'
      throw err
    } finally {
      node('master') {
        logstashSend failBuild: true, maxLines: 1000
      }
    }
  }
}
