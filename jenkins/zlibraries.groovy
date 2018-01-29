#!groovy
//https://github.com/Praqma/JenkinsAsCodeReference/tree/master/dockerizeit/master
import java.lang.System
import jenkins.model.*
import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.workflow.libs.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

//GlobalLibraries descriptor = (GlobalLibraries) GlobalLibraries.get()
//libraries = descriptor.getLibraries()
List<LibraryConfiguration> configs = new ArrayList<LibraryConfiguration>()
jenkins = Jenkins.getInstance()

println "--> Configure Pipeline shared groovy libraries."
properties.libraries.each {
  println "--> Configure Pipeline shared groovy library: ${it.value.name}"
	GitSCMSource gitSCM = new GitSCMSource(it.value.name, it.value.repo, it.value.credentials, "*", "", false)
	LibraryConfiguration globalConfig = new LibraryConfiguration(it.value.name, new SCMSourceRetriever(gitSCM))
  globalConfig.defaultVersion = it.value.version
  globalConfig.implicit = true
  globalConfig.includeInChangesets = false
  configs.add(globalConfig)
}
jenkins.getDescriptor("org.jenkinsci.plugins.workflow.libs.GlobalLibraries").get().setLibraries(configs)
jenkins.save()
Jenkins.instance.reload()
