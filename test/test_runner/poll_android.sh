#!/bin/bash

# Periodically poll Android to check if it has finished processing

timeout=$1
interval=$2
success_file=$3
error_file=$4

((end_time=${SECONDS}+timeout))

while ((${SECONDS} < ${end_time}))
do
  if [[ `adb shell ls ${success_file} 2> /dev/null` ]] 
  then
    exit 0
  fi

  if [[ `adb shell ls ${error_file} 2> /dev/null` ]]
  then
  	exit -1
  fi

  sleep ${interval}
done

exit -2
