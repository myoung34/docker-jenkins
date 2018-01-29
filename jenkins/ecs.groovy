#!groovy

import java.util.Arrays
import java.util.logging.Logger
import jenkins.model.*
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.MountPointEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.EnvironmentEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSCloud

println '--> starting ecs config'
instance = Jenkins.getInstance()
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
  image=System.getenv('JENKINS_SLAVE_IMAGE'),
  remoteFSRoot="/var/jenkins_home",
  memory=0,
  memoryReservation=2048,
  cpu=768,
  privileged=true,
  logDriverOptions=null,
  environments=null,
  extraHosts=null,
  mountPoints=mounts
)
ecsTemplate.setTaskrole(System.getenv('JENKINS_SLAVE_IAM_ROLE_ARN'))

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
