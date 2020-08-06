def read_signal(signal_file_path, multiplier=1.0):

    signal_file = open(signal_file_path, "r")
    num_samples = signal_file.readline()

    input_signal = signal_file.readlines()
    input_signal_len = num_samples

    output_signal = [0.0] * int(input_signal_len)
    output_signal_len = len(output_signal)

    for i in range(output_signal_len):
        a = float(input_signal[i].rstrip())
        output_signal[i] = a * multiplier

    return output_signal
