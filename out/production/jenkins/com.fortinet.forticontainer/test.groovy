@Library('forticasb-shared-library') _
import groovy.json.JsonBuilder
import com.fortinet.forticontainer.FortiCSForJenkins
import com.fortinet.forticontainer.HttpUploadFile

@NonCPS
def Boolean uploadImageTesting(String jobId,String imageName) {
    def tempTarFile = "curlTest:latest"
    def save = "/var/lib/jenkins/workspace/Test@libs/forticasb-shared-library/src/com/fortinet/forticontainer/saveImage.sh ${imageName} ${tempTarFile}".execute();
    save.consumeProcessOutput(sout, serr);
    save.waitForOrKill(1000);
    println("sout : ${sout}, serr : ${serr}")
    // def ctrlHost = "http://internal-fortics-controller-next-1063450219.us-east-1.elb.amazonaws.com";
    // def controllerToken = "52677600474AFBAB4BD30EEE9D7B6D28"
    // def sout = new StringBuilder(), serr = new StringBuilder()

    // def tempTarFile = "testing:latest"
    // def save = "docker save ${imageName} -o /tmp/${tempTarFile}.tar ".execute();
    
    // save.consumeProcessOutput(sout, serr);
    // save.waitForOrKill(1000);
    // println("sout : ${sout}, serr : ${serr}")

    // def imageFile = new File("/tmp/${tempTarFile}.tar");
    // if(!imageFile.exists()){
    //     println("did not fund the file ")
    //     return false;
    // }

    // def uploadFile = new HttpUploadFile(ctrlHost+"/api/v1/jenkins/image/"+jobId,controllerToken,tempTarFile);
    // def result = uploadFile.upload(imageFile);
    // // def remove = "sudo rm -rf /tmp/tempTarFile:latest.tar".execute()
    // // remove.waitFor();
    // return result;
}

// public void sendPOSTRequest(String url, String controllerToken, String attachmentFilePath)
//     {
//         String charset = "UTF-8";
//         File binaryFile = new File(attachmentFilePath);
//         String boundary = "------------------------" + Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
//         String CRLF = "\r\n"; // Line separator required by multipart/form-data.
//         int    responseCode = 0;

//         try 
//         {
//             //Set POST general headers along with the boundary string (the seperator string of each part)
//             URLConnection connection = new URL(url).openConnection();
//             connection.setDoOutput(true);
//             connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//             connection.addRequestProperty("User-Agent", "CheckpaySrv/1.0.0");        
//             connection.setRequestProperty("x-controller-token", "${controllerToken}")
            

//             OutputStream output = connection.getOutputStream();
//             PrintWriter writer  = new PrintWriter(new OutputStreamWriter(output, charset), true);

//             // Send binary file - part
//             // Part header
//             writer.append("--" + boundary).append(CRLF);
//             writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
//             writer.append("Content-Type: application/octet-stream").append(CRLF);// + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
//             writer.append(CRLF).flush();

//             // File data
//             Files.copy(binaryFile.toPath(), output);
//             output.flush(); 

//             // End of multipart/form-data.
//             writer.append(CRLF).append("--" + boundary + "--").flush();

//             responseCode = ((HttpURLConnection) connection).getResponseCode();


//             if(responseCode !=200) //We operate only on HTTP code 200
//                 return;

//         }
//         catch(Exception e)
//         {
//             e.printStackTrace();
//         }

//     }
def void sendImageToHost(String ctrlHost, String jobId, String controllerToken, String filePath) {

    def sout = new StringBuilder(), serr = new StringBuilder();
    def response = "curl --location --request POST 'http://internal-fortics-controller-next-1063450219.us-east-1.elb.amazonaws.com//api/v1/jenkins/image/12512464169340932' \
                    --header 'Content-Type: multipart/form-data' \
                    --header 'x-controller-token: 52677600474AFBAB4BD30EEE9D7B6D28' \
                    --form 'file=@/tmp/tempImage:latest.tar'".execute()
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
            
            def imageFile = new File("/tmp/tempImage:latest.tar");
            if(!imageFile.exists()){
                println("the file not found");
            }
            // def uploadFile = new HttpUploadFile(ctrlHost+"/api/v1/jenkins/image/"+jobId,controllerToken,'tempImage:latest.tar');
            // print("The uploadFile init success with boundary ${uploadFile.boundary}");
            try {
                // def result = uploadFile.upload(imageFile);
                sendImageToHost("a", "b", "c", "d");
            } catch(err) {
                println("the error message" + err);
            }    



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