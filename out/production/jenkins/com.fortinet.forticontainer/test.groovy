@Library('forticasb-shared-library') _
import groovy.json.JsonBuilder
import com.fortinet.forticontainer.FortiCSForJenkins
import com.fortinet.forticontainer.HttpUploadFile


def Boolean saveDockerimage(String imageName) {
    def sout = new StringBuilder(), serr = new StringBuilder();
    def tempTarFile = "abcImage:latest";
    // def save = "/var/lib/jenkins/workspace/Test@libs/forticasb-shared-library/src/com/fortinet/forticontainer/saveImage.sh ${imageName} ${tempTarFile}".execute();
    def save="docker save docker/whalesay -o /tmp/${tempTarFile}.tar ".execute();
    save.consumeProcessOutput(sout, serr);
    save.waitForOrKill(1000);
    println("sout : ${sout}, serr : ${serr}")
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
            try {
                def response = saveDockerimage(imageName);
                print("response : ${response}");
            } catch(err){
                println("the error message is " + err)
            }
            

            // def imageFile = new File("/tmp/tempImage:latest.tar");
            // if(!imageFile.exists()){
            //     println("the file not found");
            // }
            // def uploadFile = new HttpUploadFile(ctrlHost+"/api/v1/jenkins/image/"+jobId,controllerToken,'tempImage:latest.tar');
            // print("The uploadFile init success with boundary ${uploadFile.boundary}");
            // try {
            //     // def result = uploadFile.upload(imageFile);
            //     sendImageToHost("a", "b", "c", "d");
            // } catch(err) {
            //     println("the error message" + err);
            // }    



            // for(String image:jenkins.images){
            //     print("uploading the image name: ${image} to jobId : ${jobId}")
            //     def tempTarFile = "tempTarFile:latest"
            //     try {
            //          def uploadStatus = jenkins.uploadImage(jobId,image);
            //         sh("""
            //             ls /tmp
            //            """)
            //     } catch(err) {
            //         println("the err while uploading is " + err);
            //     }
                
            //     print("the uploading status is ${uploadStatus}");
            //     jenkins.imageResult.put(image, uploadStatus);
            // }
            // boolean status=jenkins.updateJobStatus(jobId,10);
            // print("the status has been update to jobId ${jobId} with status ${status}")

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