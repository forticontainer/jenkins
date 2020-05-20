#!/bin/bash

docker save $1 -o /tmp/$2.tar

result = $(curl --location --request POST 'http://internal-fortics-controller-next-1063450219.us-east-1.elb.amazonaws.com/api/v1/jenkins/image/12439998575153156' \
				--header 'Content-Type: multipart/form-data' \
				--header 'x-controller-token: 52677600474AFBAB4BD30EEE9D7B6D28' \
				--form file=@/tmp/$2.tar)