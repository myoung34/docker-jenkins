#!groovy

import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.AdminWhitelistRule
import jenkins.model.GlobalConfiguration
import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.Jenkins

def instance = Jenkins.getInstance()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "admin")
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)

// Initial number of executors is 2, let's decrease the number of executors to 1
instance.setNumExecutors(3)
// https://wiki.jenkins.io/display/JENKINS/CSRF+Protection
instance.setCrumbIssuer(new DefaultCrumbIssuer(true))
// More security, disable old/unsafe agent protocols. Set to hardcoded port for ELB
instance.agentProtocols = ["JNLP4-connect", "Ping"] as Set
instance.setSlaveAgentPort(50000)
// Disable CLI over the remoting protocol for security.
instance.getDescriptor("jenkins.CLI").get().enabled = false
// Disable usage statistics
instance.setNoUsageStatistics(true)
// Master use by node name label only
instance.setMode(hudson.model.Node.Mode.EXCLUSIVE)
// root url
urlConfig = JenkinsLocationConfiguration.get()
urlConfig.setUrl(System.getenv('JENKINS_URL'))
urlConfig.save()
instance.save()
