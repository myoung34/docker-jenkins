#!groovy
//https://github.com/Praqma/JenkinsAsCodeReference/tree/master/dockerizeit/master
import java.lang.System
import hudson.model.*
import jenkins.model.*
import hudson.plugins.git.*
import hudson.plugins.git.extensions.*
import javaposse.jobdsl.plugin.*
import hudson.triggers.TimerTrigger
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import jenkins.branch.BranchProperty
import jenkins.plugins.git.GitSCMSource
import jenkins.branch.DefaultBranchPropertyStrategy
import jenkins.branch.BranchSource
import org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory

def instance = Jenkins.getInstance()
def home_dir = System.getenv("JENKINS_HOME")
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("$home_dir/init.groovy.d/helpers.groovy"))
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

properties.seedjobs.each {
  println "--> Create ${it.value.name} seed jod"
  def project = null
  switch (it.value.jobType) {
    case ~/^WorkflowMultiBranchProject$/:
      project = Jenkins.instance.createProject(WorkflowMultiBranchProject.class, it.value.name)
      project.getSourcesList().add(
        new BranchSource(
          new GitSCMSource(null, it.value.repo, it.value.credentials, it.value.branches, "", false),
          new DefaultBranchPropertyStrategy(new BranchProperty[0])
        )
      )
      WorkflowBranchProjectFactory projectFactory = new WorkflowBranchProjectFactory()
      projectFactory.setScriptPath(it.value.jenkinsfile)
      project.setProjectFactory(projectFactory)
      break
    default:
      project = Jenkins.instance.createProject(WorkflowJob.class, it.value.name)
      List<UserRemoteConfig> repoList = new ArrayList<>()
      repoList.add(new UserRemoteConfig(it.value.repo, null, null, it.value.credentials))
      def scm = new GitSCM(
        repoList,
        Collections.singletonList(new BranchSpec(it.value.branches)),
        false,
        Collections.<SubmoduleConfig>emptyList(),
        null,
        null,
        Collections.<GitSCMExtension>emptyList(),
      )
      project.definition = new CpsScmFlowDefinition(scm, it.value.jenkinsfile)
      it.value.parameters.each { key, value ->
        helpers.addBuildParameter(project, key, value)
      }
      break
  }
  view = hudson.model.Hudson.instance.getView(it.value.view)
  if (view == null) {
    println "--> Create ${it.value.view} view"
    instance.addView(new ListView(it.value.view))
    view = hudson.model.Hudson.instance.getView(it.value.view)
  }
  view.doAddJobToView(it.value.name)

  project.save()
}

Jenkins.instance.reload()

properties.seedjobs.each {
  println "--> Schedule ${it.value.name} seed jod"
  Jenkins.instance.getJob(it.value.name).scheduleBuild()
}
