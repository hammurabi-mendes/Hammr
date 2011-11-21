#!/bin/bash
if [ "$1" == "--hammr_home" ]
then
    shift
    hammr_home="$1"
    shift
fi

if [ "$1" == "--master_host" ]
then
    shift
    master_host="$1"
    shift
fi

$hammr_home/scripts/hammr_daemon.sh start manager --hammr_home $hammr_home --master_host $master_host