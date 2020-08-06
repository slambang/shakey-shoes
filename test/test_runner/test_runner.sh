#!/bin/sh
# Note: Paths to other scripts are relative to this file.

input_file_local=$1
input_file_remote=$2

android_output_dir_local=$3
android_output_dir_remote=$4
android_success_file=$5
android_error_file=$6
desktop_output_dir=$7

android_poll_timeout=$8
android_poll_interval=$9

threshold=$10
buffer_size=$11
filter_center_hz=$12
filter_width_hz=$13
window_type=$14
feature_name=$15

# Run android
adb shell am start -a android.intent.action.VIEW -d "fingerband://alpha" --ef threshold ${threshold} --ei buffer_size ${buffer_size} --ei filter_center_hz ${filter_center_hz} --ei filter_width_hz ${filter_width_hz} --ei window_type ${window_type} --ei feature_name ${feature_name} --es input_file ${input_file_remote} --es output_dir ${android_output_dir_remote}

Wait for android to finish
poll_result=$(./poll_android.sh ${android_poll_timeout} ${android_poll_interval} ${android_success_file} ${android_error_file})
if ( ${poll_result} != 0 ) ; then
 exit -1
fi

Pull the results from android
adb pull ${android_output_dir_remote}/mono ${android_output_dir_local}
adb pull ${android_output_dir_remote}/filtered ${android_output_dir_local}
adb pull ${android_output_dir_remote}/magnitude ${android_output_dir_local}
adb pull ${android_output_dir_remote}/event ${android_output_dir_local}
adb pull ${android_output_dir_remote}/log ${android_output_dir_local}
adb pull ${android_output_dir_remote}/success ${android_output_dir_local}

# Run desktop
echo ${input_file_local} ${desktop_output_dir} ${threshold} ${buffer_size} ${filter_center_hz} ${filter_width_hz} ${window_type} ${feature_name}
../../common/libfingerband/libfingerband_desktop_harness/cmake-build-debug/fingerband ${input_file_local} ${desktop_output_dir} ${threshold} ${buffer_size} ${filter_center_hz} ${filter_width_hz} ${window_type} ${feature_name}

# Render graph reuslt
python ../main.py ${desktop_output_dir} ${android_output_dir_local}
