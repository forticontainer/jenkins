@Library('forticasb-shared-library') _
import groovy.json.JsonBuilder
import com.fortinet.forticontainer.FortiCSForJenkins


node {

    ctrlHost = "http://172.30.154.23:10023";
    jenkinsHost = "${env.JOB_URL}";
    projectName = "${env.JOB_NAME}";
    buildNumber = "${env.BUILD_NUMBER}";
    // def userName = "${env.BUILD_USER_ID}";
    def imageName = "482025328369.dkr.ecr.us-east-1.amazonaws.com/fortics-controller:next-3";
    controllerToken = "52677600474AFBAB4BD30EEE9D7B6D28"


    def currentJobResult = "${currentBuild.currentResult}"
    echo "jenkins hot : " + jenkinsHost
    echo "project name : " + projectName
    echo "build number : " + buildNumber

    try {

        stage('Preparation') { // for display purposes
            // Get some code from a GitHub repository
            echo "preparation "
            sleep 2
        }
        stage('Build') {
            echo "build docker"
            //imageName.add()  //add the image name
            sleep 2
        }

        stage("image scan") {
            echo "new jenkins plugin";
            FortiCSForJenkins jenkinsPlugin = new FortiCSForJenkins(ctrlHost,controllerToken,imageName,jenkinHost,projectName,buildNumber);
            echo "add image";
            jenkinsPlugin.imageName=imageName;
            echo "image scan start";
            int result= imageScan();
            echo "${result}";
            if(result>0){
                currentBuild.result = 'FAILURE';
                // exit 1
            }
        }

        stage("other processing") {
            echo "other processing";
        }
    } catch(err) {

    }finally {

    }
}
