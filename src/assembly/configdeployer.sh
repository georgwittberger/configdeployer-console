#!/bin/bash 
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
java -cp "$SCRIPTPATH/lib/*" com.configdeployer.console.ConfigDeployer $*