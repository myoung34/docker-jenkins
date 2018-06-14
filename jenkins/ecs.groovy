#!groovy

import java.util.Arrays
import java.util.logging.Logger
import jenkins.model.*
import com.amazonaws.services.ecs.model.*;
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.MountPointEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.EnvironmentEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.LogDriverOption
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.PortMappingEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSCloud

println '--> starting ecs config'
instance = Jenkins.getInstance()
println '--> creating logdriver opts'
def logDriverOpts = Arrays.asList(
  new LogDriverOption(
    name="syslog-address",
    value='tcp://172.17.0.1:5514'),
  new LogDriverOption(
    name="tag",
    value='jenkins-slave'),
)
println '--> creating environments'
def environments = Arrays.asList(
  new EnvironmentEntry(
    name="VAULT_ADDR",
    value=System.getenv('VAULT_ADDR')),
  new EnvironmentEntry(
    name="VAULT_SKIP_VERIFY",
    value=System.getenv('VAULT_SKIP_VERIFY')),
)
println '--> creating mounts'
def mounts = Arrays.asList(
  new MountPointEntry(
    name="docker",
    sourcePath="/var/run/docker.sock",
    containerPath="/var/run/docker.sock",
    readOnly=false),
  new MountPointEntry(
    name="jenkins-workspace",
    sourcePath="/var/jenkins_home",
    containerPath="/var/jenkins_home",
    readOnly=false),
)

def ecsTemplate = new ECSTaskTemplate(
  templateName="jnlp-slave-with-docker",
  label="ecs docker any ec2-deploy ec2 deploy",
  taskDefinitionOverride=null,
  image=System.getenv('JENKINS_SLAVE_IMAGE'),
  launchType="EC2",
  remoteFSRoot="/var/jenkins_home",
  memory=0,
  memoryReservation=1536,
  cpu=1,
  subnets="",
  securityGroups="",
  assignPublicIp=false,
  privileged=true,
  containerUser="",
  logDriverOptions=logDriverOpts,
  environments=environments,
  extraHosts=null,
  mountPoints=mounts,
  portMappings=null
)
ecsTemplate.setTaskrole(System.getenv('JENKINS_SLAVE_IAM_ROLE_ARN'))
ecsTemplate.setLogDriver("syslog")

ecsCloud = new ECSCloud(
  name="ecs",
  templates=Arrays.asList(ecsTemplate),
  credentialsId=null,
  cluster=System.getenv('ECS_CLUSTER_ARN'),
  regionName=System.getenv('AWS_REGION'),
  jenkinsUrl=null,
  slaveTimoutInSeconds=60
)

def clouds = instance.clouds
println '--> adding cloud'
clouds.add(ecsCloud)
println '--> saving'
instance.save()
