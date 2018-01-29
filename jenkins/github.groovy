#!groovy
import jenkins.model.*
import jenkins.branch.OrganizationFolder
import jenkins.branch.MultiBranchProject
//import hudson.triggers.TimerTrigger
import com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger
import com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy
import org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator
import org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait
import jenkins.scm.impl.trait.WildcardSCMHeadFilterTrait
import hudson.util.Secret
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.common.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

// Create the credentials used to access GitHub.
println '--> Creating GitHub access token credential'
githubUser = System.getenv('GITHUB_USER')
githubToken = System.getenv('GITHUB_TOKEN')
//////// PROJECT FOLDER
println '--> Creating organization folder'
def githubOrg = System.getenv('GITHUB_ORG')
def folder = Jenkins.instance.createProject(OrganizationFolder, githubOrg)
def navigator = new GitHubSCMNavigator(githubOrg)

if (githubUser != null && githubUser != '' && githubToken != null && githubToken != '') {
  cred = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "github_token",
    "GitHub access token",
    githubUser,
    githubToken)
  SystemCredentialsProvider.instance.store.addCredentials(Domain.global(), cred)
  navigator.credentialsId = cred.id // Loaded above in the GitHub section.
}

navigator.traits = [
  new jenkins.scm.impl.trait.RegexSCMSourceFilterTrait('docker-jenkins'),
  new BranchDiscoveryTrait(3), // Exclude branches that are also filed as PRs.
  new WildcardSCMHeadFilterTrait("*", null),
]
folder.navigators.replace(navigator)

folder.addTrigger(new PeriodicFolderTrigger("5m"))
folder.setOrphanedItemStrategy(new DefaultOrphanedItemStrategy(true, 7, -1))
folder.save()

println '--> Scheduling GitHub organization scan'
Thread.start {
  sleep 30000 // 30 seconds
  println '--> Running GitHub organization scan'
  folder.scheduleBuild()
}
