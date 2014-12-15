#!/bin/bash

# This script sets appropriately the
# system variables that are needed in
# order to run Hadoop and pegasus 
# (http://www.cs.cmu.edu/~pegasus/).
# OS supported: Darwin (mac) and Linux

# ******************************************
# ATTENTION: *every* time you run this
#            script some entries are 
#            added at the end of .bashrc
#            It's YOUR responsibility to
#            delete the additional entries
#            if you run the script more 
#            than once.
# ******************************************


lowercase(){
	    echo "$1" | sed "y/ABCDEFGHIJKLMNOPQRSTUVWXYZ/abcdefghijklmnopqrstuvwxyz/"
}

############################################
# STEP 1: Setting up Hadoop configuration  #
# (a) separate configuration for Mac/Linux #
############################################

echo "STEP 1: Setting up HADOOP configuration..."

OS=`lowercase \`uname\``
echo "         OS = $OS"
MAC="darwin"
LINUX="linux"

if [[ "$OS" == "$MAC" ]]; then
   # mac
   javaHomeWhole=`/usr/libexec/java_home`
   len=`echo $javaHomeWhole | sed -n "s/jdk.*//p" | wc -c`
   let "len=len+2"
   javaHomeFirst=`echo ${javaHomeWhole:0:$len}`
   let "len=len+1"
   javaHomeSecond=`echo ${javaHomeWhole:$len}`	

   # Editing DEMO/hadoop-0.20.203.0/conf/hadoop-env.sh
   sed "s#.*export JAVA_HOME=.*#export JAVA_HOME=$javaHomeFirst#" hadoop-0.20.203.0/conf/hadoop-env.sh > tmp.txt
   mv tmp.txt hadoop-0.20.203.0/conf/hadoop-env.sh

   # Editing DEMO/hadoop-0.20.203.0/bin/hadoop
   sed "s#.*JAVA=\$JAVA_HOME/bin/java.*#JAVA_HOME=\$JAVA_HOME/$javaHomeSecond#" hadoop-0.20.203.0/bin/hadoop > tmp.txt
   mv tmp.txt hadoop-0.20.203.0/bin/hadoop
######################################################################################
elif [[ "$OS" == "$LINUX" ]] ; then
   # linux
   echo $OS
   javaHomeWhole=`which java`
   toExcludePath="/bin/java"
   echo "$toExcludePath"
   javaHomeFirst=${javaHomeWhole%$toExcludePath}

   # Editing DEMO/hadoop-0.20.203.0/conf/hadoop-env.sh
   sed "s#.*export JAVA_HOME=.*#export JAVA_HOME=$javaHomeFirst#" hadoop-0.20.203.0/conf/hadoop-env.sh > tmp.txt
   mv tmp.txt hadoop-0.20.203.0/conf/hadoop-env.sh
######################################################################################
else
   echo -e "\n\x1B[00;31m Operating system not supported for automatic configuration.\n \x1B[00m"
   echo -e "\x1B[00;31m Please try configuring the files for running Hadoop manually.\n\x1B[00m"
fi


############################################
# STEP 2: Setting up Hadoop configuration  #
# (b) joint configuration for Mac/Linux    #
############################################

echo "STEP 2: Adding variables in home/.bashrc"

# Editing .bashrc
homeDir=`echo $HOME`
currentDir=`echo $PWD`
echo $currentDir
echo "export JAVA_HOME=$javaHomeWhole" >> $homeDir/.bashrc
echo "export HADOOP_HOME=$currentDir/hadoop-0.20.203.0" >> $homeDir/.bashrc
echo "export PATH=\${HADOOP_HOME}/bin:\${PATH}" >> $homeDir/.bashrc
echo "export HADOOP_VERSION=0.20.203.0" >> $homeDir/.bashrc

echo -e "\n\x1B[00;33m Finished setting up the configuration...\n \x1B[00m"


