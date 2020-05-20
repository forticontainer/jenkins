#!/bin/bash

#docker save $1 -o /tmp/$2.tar

curl --location --request POST 'http://internal-fortics-controller-next-1063450219.us-east-1.elb.amazonaws.com//api/v1/jenkins/image/12512464169340932' --header 'Content-Type: multipart/form-data' --header 'x-controller-token: 52677600474AFBAB4BD30EEE9D7B6D28' --form 'file=@/tmp/tempImage:latest.tar')
#echo $result