#!/bin/bash

wait_android_finish() {

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
}

# Run android
adb shell am start -a android.intent.action.VIEW -d "fingerband://alpha" --ef threshold ${threshold} --ei buffer_size ${buffer_size} --ei filter_center_hz ${filter_center_hz} --ei filter_width_hz ${filter_width_hz} --ei window_type ${window_type} --ei feature_name ${feature_name} --es input_file ${input_file_remote} --es output_dir ${android_output_dir_remote}

# Wait for android to finish
poll_result=wait_android_finish($1 $2 $3 $4)
if ( ${poll_result} != 0 ) ; then
  exit -1
fi

# Pull the results from android to local machine
adb pull ${android_output_dir_remote}/mono ${android_output_dir_local}
adb pull ${android_output_dir_remote}/filtered ${android_output_dir_local}
adb pull ${android_output_dir_remote}/magnitude ${android_output_dir_local}
adb pull ${android_output_dir_remote}/event ${android_output_dir_local}
adb pull ${android_output_dir_remote}/log ${android_output_dir_local}
adb pull ${android_output_dir_remote}/success ${android_output_dir_local}
