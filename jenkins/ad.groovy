#!groovy

// AD
import jenkins.model.*
import hudson.security.*
import hudson.plugins.active_directory.*

def instance = Jenkins.getInstance()
String domain = System.getenv('AD_BIND_DOMAIN')
if (domain != null && domain != "") {
  println '--> Creating the AD realm'
  String site = ''
  String server = System.getenv('AD_BIND_SERVER')
  String bindName = System.getenv('AD_BIND_DN')
  String bindPassword = System.getenv('AD_BIND_PASSWORD')
  adrealm = new ActiveDirectorySecurityRealm(domain, site, bindName, bindPassword, server)
  println '--> setting auth to use AD'
  instance.setSecurityRealm(adrealm)
}
