#!groovy
import jenkins.model.*
import hudson.plugins.slack.*
import hudson.util.Secret
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.common.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

def instance = Jenkins.getInstance()

credentials = new StringCredentialsImpl(
  CredentialsScope.GLOBAL,
  "jenkins_slack_token",
  "slack token",
  new Secret(System.getenv('SLACK_TOKEN')))
SystemCredentialsProvider.instance.store.addCredentials(Domain.global(), cred)

def _slack = instance.getDescriptorByType(jenkins.plugins.slack.SlackNotifier.DescriptorImpl)
_slack.teamDomain = System.getenv('SLACK_DOMAIN')
_slack.room = System.getenv('SLACK_CHANNEL')
_slack.token = System.getenv('SLACK_TOKEN')
//_slack.setAuthTokenCredentialId(credentials.id)
_slack.save()
