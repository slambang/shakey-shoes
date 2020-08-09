def plot_magnitudes_(magnitudes, window_size, mono_signal_size):

    window_offset = int(window_size / 2)
    output = [0.0] * (mono_signal_size + window_offset)

    for i in range(len(magnitudes)):
        output[window_offset] = float(magnitudes[i])
        window_offset = window_offset + window_size

    return output


def read_average_magnitudes(path, mono_signal_size):

    magnitudes_file = open(path, "r")

    window_size = int(magnitudes_file.readline())
    magnitudes = magnitudes_file.readlines()

    plot = plot_magnitudes_(magnitudes, window_size, mono_signal_size)
    return plot
