package com.fortinet.forticontainer

import groovy.json.JsonBuilder
/**
 *     def jenkinsHost = "${env.JOB_URL}";
 *     def projectName = "${env.JOB_NAME}";
 *     def buildNumber = "${env.BUILD_NUMBER}";
 *     // def userName = "${env.BUILD_USER_ID}";
 *     def ctrlHost = "http://172.30.154.23:10023";
 *     def imageName = "482025328369.dkr.ecr.us-east-1.amazonaws.com/fortics-controller:next-3";
 *     def controllerToken = "52677600474AFBAB4BD30EEE9D7B6D28"
 */

class FortiCSForJenkins {
    private String ctrlHost;
    private String controllerToken;
//    private List<String> images=new ArrayList<>();
    private String jenkinsHost;
    private String projectName;
    private String buildNumber;
    String imageName

    FortiCSForJenkins(ctrlHost,ctrlToken,jenkinsHost,projectName,buildNumber){
        println("testtest");
        this.ctrlHost = ctrlHost;
        this.controllerToken = ctrlToken;
        this.jenkinsHost = jenkinsHost;
        this.projectName = projectName;
        this.buildNumber = buildNumber;
//        this.images = new ArrayList<>();
    }

//    def void addImage(String image){
//        this.images.add(image);
//    }



    def String addJob() {
        def desc = sh("""docker images""")
        println(desc);
        def jsonBody = ["jobName" : "${projectName}",
                        "jobHost": "${jenkinsHost}",
                        "buildNumber": "${buildNumber}"]
        println("start add jenkins job");
        def post = new URL("${ctrlHost}/api/v1/jenkins/job").openConnection();
        def builder = new JsonBuilder()
        builder(jsonBody)
        def body = builder.toString();

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.setRequestProperty("x-controller-token", "${controllerToken}")
        post.getOutputStream().write(body.getBytes("UTF-8"));
        def postRC = post.getResponseCode();
        println("the post rc is :" + postRC);
        if(postRC.equals(200)) {
            return post.getInputStream().getText();
        }else{
            println("add job error ")
        }
        return "";
    }

    def Boolean uploadImage(String jobId,String imageName) {
        sh("""docker save ${imageName} -o /tmp/tmp_image.tar """)
        sh("""ls -lh /tmp/tmp*""")
        sh("""curl --location --request POST '${ctrlHost}/api/v1/jenkins/image/${jobId}' \
                 -H 'Content-Type: multipart/form-data' \
                 -H 'x-controller-token: ${controllerToken}' \
                 -H 'imageName: ' \
                 --form 'file=@/tmp/tmp_image.tar'   
             """).trim()
        sh("""rm -rf /tmp/tmp_image.tar""")
        return true;
    }

    def Boolean updateJobStatus(String jobId,Integer status){
        def jsonBody = ["status": "${status}"]
        def post = new URL("${ctrlHost}/api/v1/jenkins/job/${jobId}").openConnection();
        def builder = new JsonBuilder()
        builder(jsonBody)
        def body = builder.toString();
        println('the body is ' + body)

        post.setRequestMethod("PUT")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.setRequestProperty("x-controller-token", "${controllerToken}")
        post.getOutputStream().write(body.getBytes("UTF-8"));
        def postRC = post.getResponseCode();
        if(postRC.equals(200)) {
            //do something here
            println("success update jenkins");
            return true;
        }else{
            return false;
        }
    }

// @NonCPS
    def int checkResult(String jobId) {
        int result=0;
        int status=0;
        println("Start check");
        def statusResponse = sh(returnStdout: true, script: """
              curl --location --request GET '${ctrlHost}/api/v1/jenkins/job/${jobId}' \
                  --header 'x-controller-token: ${controllerToken}'
          """).trim();

        def jsonMap = new groovy.json.JsonSlurper().parseText(statusResponse);
        result = jsonMap['result'];
        status = jsonMap['status'];
        print("the result is ${result}, the status is ${status}");

        if(status<20){
            return 0
        }else{
            return result
        }

    }

    def int imageScan(){
        println( "jenkins hot : " + jenkinsHost);
        println( "project name : " + projectName);
                println( "build number : " + buildNumber);

        try {
            jobId=addJob(ctrlHost, controllerToken, projectName, jenkinsHost, buildNumber);
            if(jobId==""){
                echo "add job fail";
                return 20; //todo add job fail
            }

            println( "2.1 save docker image "+jobId);
//            for(String image:images){
//                uploadImage(jobId,image);
//            }
            uploadImage(jobId,imageName);
            boolean status=updateJobStatus(jobId,10);
            if(status!=true){
                println( "fail");
                currentBuild.result = 'FAILURE'
                return 10; //todo update status fail
            }else{
                println( "testing");
                println( "success update jenkins job status");
            }
            int result = 0;
            timeout(time: 30, unit: 'MINUTES') {
                while(result <= 0) {
                    result = checkResult(jobId);
                    sleep 10;
                }
            }
            return result;
        } catch(err) {

        }finally {

        }
        return 30;
    }
}

