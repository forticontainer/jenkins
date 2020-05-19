@Library('forticasb-shared-library') _
import groovy.json.JsonBuilder
import com.fortinet.forticontainer.FortiCSForJenkins


def Boolean uploadImageTesting(String jobId,String imageName) {
    def tempTarFile = "tempTarFile:latest"
    def save="docker save ${imageName} -o /tmp/${tempTarFile}.tar ".execute();
    save.waitFor();
    println save.text;

    def imageFile = new File("/tmp/${tempTarFile}.tar");
    if(!imageFile.exists()){
        return false;
    }

    def uploadFile = new HttpUploadFile(ctrlHost+"/api/v1/jenkins/image/"+jobId,controllerToken,tempTarFile);
    def result = uploadFile.upload(imageFile);
    def remove = "rm -rf /tmp/tempTarFile:latest.tar".execute()
    remove.waitFor();
    return result;
}
node {

    // ctrlHost = "http://172.30.154.23:10023";
    ctrlHost = "http://internal-fortics-controller-next-1063450219.us-east-1.elb.amazonaws.com";
    jenkinsHost = "${env.JOB_URL}";
    projectName = "${env.JOB_NAME}";
    buildNumber = "${env.BUILD_NUMBER}";
    // def userName = "${env.BUILD_USER_ID}";
    controllerToken = "52677600474AFBAB4BD30EEE9D7B6D28"


    def currentJobResult = "${currentBuild.currentResult}"
    echo "jenkins hot : " + jenkinsHost
    echo "project name : " + projectName
    echo "build number : " + buildNumber
    def jenkins = new FortiCSForJenkins();
    jenkins.ctrlHost=ctrlHost;
    jenkins.controllerToken=controllerToken;
    jenkins.jenkinsHost=jenkinsHost;
    jenkins.projectName=projectName;
    jenkins.buildNumber=buildNumber;
    

timestamps{
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
            echo "add image";
            
        }

        stage("image scan") {
            echo "new jenkins plugin";
            // def imageName = "482025328369.dkr.ecr.us-east-1.amazonaws.com/fortics-controller:next-3";
            def imageName = "docker/whalesay"
            println("The init jenkins Successful with ctrlHost : ${jenkins.ctrlHost}, projectName ： ${projectName}")
            jenkins.images.add(imageName)
            println("the image has been add to jenkins ${jenkins.images}")
            
            def jobId = jenkins.addJob();
            println("the job id is ${jobId}");
           
            println("save 2.1 save docker image ${jobId}");
            for(String image:jenkins.images){
                print("uploading the image ${image} to jobId ${jobId}")
                def uploadStatus = uploadImageTesting(jobId,image);
                print("the uploading status is ${uploadStatus}");
                jenkins.imageResult.put(image, uploadStatus);
            }
            boolean status=updateJobStatus(jobId,10);
            print("the status has been update to jobId ${jobId} with status ${status}")

            // def result = jenkins.imageScan();
            println("the image scan result is ${result}");
            println("fail message : "+jenkins.message);
            println(jenkins.imageResult);
            if(!result){
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
}