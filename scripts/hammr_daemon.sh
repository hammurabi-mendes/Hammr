#!/bin/bash

# Runs Hammr Manager
# Environment Variables
#
# HAMMR_HOME
#
#
#

usage="Usage: hammr_daemon.sh (start|stop) (manager|launcher) [--hammr_home hammr_home_dir] [--master master_hostname]"

startStop=$1
shift
name=$1
shift

if [ "$1" == "--hammr_home" ]
then
    shift
    hammr_home=$1
    shift
fi

if [ "$1" == "--master_host" ] 
then
    shift
    manger_host=$1
    shift
fi



BASEDIR=$hammr_home
REGISTRYLOC=$manger_host

LIBDIR="$BASEDIR/lib/"
JGRAPHFILE="$LIBDIR/jgrapht-jdk1.6.jar"
HADOOPJAR="$LIBDIR/hadoop-core-0.20.2-cdh3u1.jar"
#HDFSJAR="$LIBDIR/hadoop-hdfs-0.21.0.jar"
#HADOOPCOMMONJAR="$LIBDIR/hadoop-common-0.21.0.jar"
COMMONLOGGINGJAR="$LIBDIR/commons-logging-1.1.1.jar"

BINDIRS=$BASEDIR/Common/bin/:$BASEDIR/Client/bin:$BASEDIR/Launcher/bin:$BASEDIR/Manager/bin

HEAPPARAMS="-XX:-UseGCOverheadLimit -Xms250M -Xmx1000M"

CODEBASE="file://${BASEDIR}/Common/bin/"

#export CLASSPATH=$BINDIRS:$JGRAPHFILE:$HDFSJAR:$HADOOPCOMMONJAR:$COMMONLOGGINGJAR
export CLASSPATH=$BINDIRS:$JGRAPHFILE:$HADOOPJAR:$COMMONLOGGINGJAR

HAMMR_PID_DIR=/tmp
HAMMR_LOG_DIR=$BASEDIR/log
HAMMR_IDENT_STRING="$USER"
pid=$HAMMR_PID_DIR/hammr-$HAMMR_IDENT_STRING-$name.pid
log=$HAMMR_LOG_DIR/hammr-$HAMMR_IDENT_STRING-$name-$(hostname).log

case $startStop in
    (start)
        mkdir -p "$HAMMR_PID_DIR"
        if [ -f $pid ]; then
            echo $name running as process $(cat $pid). Stop it first.
            exit 1
        fi

        case $name in
            (manager)
                echo "Running manager..."
		echo "MasterHost: $REGISTRYLOC"
		cmd="java -cp $CLASSPATH $HEAPPARAMS -Djava.rmi.server.codebase=$CODEBASE -Djava.security.policy=$BASEDIR/Manager/security.policy -Djava.rmi.server.location=$REGISTRYLOC -Dhammr.hdfs.uri=hdfs://$REGISTRYLOC:9000 manager.ConcreteManager > $log 2>&1 &"
		echo $cmd
		java -cp $CLASSPATH $HEAPPARAMS -Djava.rmi.server.codebase=$CODEBASE -Djava.security.policy=$BASEDIR/Manager/security.policy -Djava.rmi.server.location=$REGISTRYLOC -Dhammr.manager.basedir=$BASEDIR -Dhammr.hdfs.uri=hdfs://$REGISTRYLOC:9000 manager.ConcreteManager > $log 2>&1 &
                echo $! > $pid
		#wait for the manager to come up
		sleep 10
                ;;

            (launcher)
                echo "Running launcher..."
		cmd="java -cp $CLASSPATH $HEAPPARAMS -Djava.rmi.server.codebase=$CODEBASE -Djava.security.policy=$BASEDIR/Launcher/security.policy -Djava.rmi.server.location=$REGISTRYLOC -Dhammr.hdfs.uri=hdfs://$REGISTRYLOC:9000 launcher.ConcreteLauncher > $log 2>&1 &"
		echo $cmd
		java -cp $CLASSPATH $HEAPPARAMS -Djava.rmi.server.codebase=$CODEBASE -Djava.security.policy=$BASEDIR/Launcher/security.policy -Djava.rmi.server.location=$REGISTRYLOC -Dhammr.hdfs.uri=hdfs://$REGISTRYLOC:9000 launcher.ConcreteLauncher > $log 2>&1 &
                echo $! > $pid
                ;;
        esac
        ;;
    
    (stop)
        if [ -f $pid ]; then
            echo Stopping $name...Pid $(cat $pid).
            kill $(cat $pid)
	    rm $pid
        fi
        ;;
esac    
