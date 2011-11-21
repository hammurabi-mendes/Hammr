#!/bin/bash
if [ "$1" == "--hammr_home" ]
then
    shift
    hammr_home="$1"
    shift
fi

$hammr_home/scripts/hammr_daemon.sh stop manager --hammr_home $hammr_home