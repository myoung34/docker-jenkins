#!groovy
import jenkins.model.*
import com.cloudbees.syslog.sender.UdpSyslogMessageSender
import jenkins.plugins.logstash.*
import jenkins.plugins.logstash.persistence.*
import hudson.tools.ToolProperty

def instance = Jenkins.getInstance()

println('-----> logstash start')
def _logstash = Jenkins.getInstance().getDescriptor(LogstashInstallation.class)
_logstash.type = jenkins.plugins.logstash.persistence.LogstashIndexerDao.IndexerType.ELASTICSEARCH
_logstash.host = System.getenv('ELASTICSEARCH_HOST')
_logstash.port = System.getenv('ELASTICSEARCH_PORT') as Integer
_logstash.username = System.getenv('ELASTICSEARCH_USERNAME')
_logstash.password = System.getenv('ELASTICSEARCH_PASSWORD')
_logstash.key = System.getenv('ELASTICSEARCH_KEY')
_logstash.save()
println('-----> logstash end')
