#!/bin/bash


sudo java -jar  ../IntelliMatchAPI-0.0.1-SNAPSHOT.jar --spring.config.location=file:/home/ec2-user/intellimatch/config/application-ec2.properties & echo $! > ./pid.file &
