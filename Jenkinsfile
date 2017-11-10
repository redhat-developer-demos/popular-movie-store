node("mavenwithnexus") {
  checkout scm

  def isCanary = false
  def isTimeout = false

  try {
    timeout(time: 24,  unit: 'HOURS') {
      isCanary = input( id:'CanaryDeployment', message: 'Do a Canary Release ?',
      parameters: [
        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '',
                  name: 'Please confirm to do a canary release']
      ])
    }
  }catch(err) {
     def user = err.getCauses()[0].getUser()
     if('SYSTEM' == user.toString()){
       isTimeout = true
     }else{
        isCanary  = false
        echo "Aborted by: [${user}]"
     }
  }

  if(isTimeout) {
     echo 'Since no confirmation recevied on time, doing nothing'
     currentBuild.result  = 'FAILURE'
  }else{
    if(isCanary) {
      stage("Test") {
        sh "mvn -B  test"
      }
      stage("Deploy") {
          sh "mvn  -DskipTests clean -Pcanary fabric8:deploy"
      }
    }else{
      stage("Test") {
        sh "mvn -B  test"
      }
      stage("Deploy") {
          sh "mvn  -DskipTests clean fabric8:deploy"
      }
    }
  }

}