#!/bin/bash

#skip checkstyle skip findbugs skip tests download sources download docs and execute main class
mvn clean install -Dcheckstyle.skip -Dfindbugs.skip -Dmaven.test.skip dependency:sources dependency:resolve -Dclassifier=javadoc exec:exe

mvn clean install -Dcheckstyle.skip -Dfindbugs.skip