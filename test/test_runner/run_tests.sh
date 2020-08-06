#!/bin/bash

input_wav=drum_loop_16bit_signed_pcm.wav
#input_wav=drum_loop_16bit_signed_pcm_mono.wav
#input_wav=terminus_signed_16bit_pcm.wav
#input_wav=wagon_wheel_16bit_PCM.wav

samples_dir_local=/home/steve/Desktop/the_shoes/test/test_data
samples_dir_remote=/sdcard/fingerband/samples

output_base=/home/steve/Desktop/the_shoes/test/test_runner/results
android_base=${output_base}/android
desktop_base=${output_base}/desktop

remote_output_dir=/sdcard/fingerband/output
remote_samples_dir=/sdcard/fingerband/samples

tear_down() {
  rm -r $android_base 2> /dev/null
  mkdir $android_base

  rm -r $desktop_base 2> /dev/null
  mkdir $desktop_base
}

test_number=0
run_test() {
  test_number=$((test_number+1))
  android_test_dir=${android_base}/${test_number}
  desktop_test_dir=${desktop_base}/${test_number}
  mkdir ${android_test_dir}
  mkdir ${desktop_test_dir}

#  adb shell rm -r ${remote_output_dir} 2> /dev/null
#  adb shell mkdir ${remote_output_dir}

  test_result=$(./test_runner.sh \
	${samples_dir_local}/${input_wav} \
	${remote_samples_dir}/${input_wav} \
	${android_test_dir} \
	${remote_output_dir} \
	${remote_output_dir}/success \
	${remote_output_dir}/error \
	${desktop_test_dir} \
	120 \
	1 \
	${1} \
	${2} \
	${3} \
	${4} \
	${5} \
	${6})

  echo 'Result: ' ${test_result}
}

tear_down
run_test 0.5 1024 200 1 1 3
