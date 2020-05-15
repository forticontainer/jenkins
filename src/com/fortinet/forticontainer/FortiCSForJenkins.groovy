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
    def String ctrlHost;
    def String controllerToken;
    def List<String> images=new ArrayList<>();
    def String jenkinsHost;
    def String projectName;
    def String buildNumber;

    def Map<String,String> imageResult=new HashMap<>();
    def String message="no message";

    FortiCSForJenkins(){
        println("testtest");

    }



    @NonCPS
    def String addJob() {
        def desc = "docker images".execute();
        println(desc.text);
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
    
    @NonCPS
    def Boolean uploadImage(String jobId,String imageName) {
        def save="docker save ${imageName} -o /tmp/${imageName}.tar ".execute();
        save.waitFor();
        println save.text;
        def lsResult = "ls -lh /tmp/".execute();
        println(lsResult.text);

        def imageFile = new File("/tmp/${imageName}.tar");
        if(!imageFile.exists()){
            return false;
        }

        def uploadFile = new HttpUploadFile(ctrlHost+"/api/v1/jenkins/image/"+jobId,controllerToken,imageName);
        def result = uploadFile.upload(imageFile);
        def remove = "rm -rf /tmp/tmp_image.tar".execute()
        remove.waitFor();
        return result;
    }
    @NonCPS
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

    @NonCPS
    def int checkResult(String jobId) {
        int result=0;
        int status=0;
        println("Start check");

        def get = new URL("${ctrlHost}/api/v1/jenkins/job/${jobId}").openConnection();
        def builder = new JsonBuilder()
        get.setRequestMethod("GET")
        get.setDoOutput(true)
        get.setRequestProperty("Content-Type", "application/json")
        get.setRequestProperty("x-controller-token", "${controllerToken}")
        def getRC = get.getResponseCode();
        if(getRC.equals(200)) {
            def jsonMap = new groovy.json.JsonSlurper().parseText(get.getInputStream().getText());
            result = jsonMap['result'];
            status = jsonMap['status'];
            println("the result is ${result}, the status is ${status}");
            /**
             *  UNDONE(0),
             *  FAIL(1),
             *  PASS(5),
             *     CANCEL(10);
             */
            if(status<20){
                return 0;
            }
        }
        return result;
    }
    @NonCPS
    def boolean imageScan(){
        println( "jenkins hot : " + jenkinsHost);
        println( "project name : " + projectName);
        println( "build number : " + buildNumber);
        int result = 0;
        try {
            def jobId=addJob();
            if(jobId==""){
                println("add job fail");
                message = "add job fail";
                return false; //todo add job fail
            }

            println( "2.1 save docker image "+jobId);
//            for(String image:images){
//                uploadImage(jobId,image);
//            }
            for(String imageName:images){
                imageResult.put(imageName,uploadImage(jobId,imageName));
            }
            boolean status=updateJobStatus(jobId,10);
            if(status!=true){
                println( "fail");
                message = "update fail";
                return false; //todo update status fail
            }
            while(result <= 0) {
                result = checkResult(jobId);
                if(result>0){
                    break;
                }
                sleep 5*1000;
            }
            /**
             UNDONE(0),
             FAIL(1),
             PASS(5),
             CANCEL(10);
             */
            if(result==5){
                return true;
            }
            message = result;
        } catch(err) {
            message=err.getMessage();
            println(err)
        }
        return false;
    }

    public static void main(String[] arg){
        def ctrlHost = "http://172.30.154.23:10023";
//        def ctrlHost = "http://127.0.0.1:8000";
        def jenkinsHost = "test";
        def projectName = "test";
        def buildNumber = "012";
        // def userName = "${env.BUILD_USER_ID}";
        def imageName = "redis:latest";
        def controllerToken = "52677600474AFBAB4BD30EEE9D7B6D28"

       def jenkins = new FortiCSForJenkins();
        jenkins.ctrlHost=ctrlHost;
        jenkins.controllerToken=controllerToken;
        jenkins.jenkinsHost=jenkinsHost;
        jenkins.projectName=projectName;
        jenkins.buildNumber=buildNumber;
        jenkins.images.add(imageName);

        def result = jenkins.imageScan();
        println(result);
        println("fail message : "+jenkins.message);
        println(jenkins.imageResult);

    }
}

