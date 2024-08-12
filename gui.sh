#!/bin/bash

MEM="-Xmx512m";
if [[ $1 == -Xmx* ]]; then
  # this is for the VM
  MEM=$1
  shift
fi

# change dir
pwd=`pwd`
path=`case $0 in /*) echo $0;; *) echo $pwd/$0;; esac`
dir=`dirname $path`

java -cp $dir/lib/procope-1.2.jar $MEM procope.userinterface.gui.GUIMain $dir $*
