#!groovy
import jenkins.model.*
import com.cloudbees.syslog.sender.UdpSyslogMessageSender
import jenkins.plugins.logstash.*
import jenkins.plugins.logstash.persistence.*
import jenkins.plugins.logstash.configuration.*
import com.cloudbees.syslog.MessageFormat;
import jenkins.plugins.logstash.persistence.LogstashIndexerDao.SyslogProtocol;
import hudson.tools.ToolProperty

config = GlobalConfiguration.all().get(LogstashConfiguration.class);
Syslog syslog = new Syslog();
syslog.setHost('172.17.0.1');
syslog.setPort(5555);
syslog.setSyslogProtocol(SyslogProtocol.UDP);
syslog.setMessageFormat(MessageFormat.RFC_5424);

config.setLogstashIndexer(syslog);
config.save();
println('-----> logstash end')
