import sys
import filecmp
import matplotlib.pyplot as plt
from event_reader import read_events
from signal_reader import read_signal
from average_magnitude_reader import read_average_magnitudes
# import numpy as np


# y = [2, 4, 6, 8, 10, 12, 14, 16, 18, 20]
# y2 = [10, 11, 12, 13, 14, 15, 16, 17, 18, 19]
# x = np.arange(10)
# fig = plt.figure()
# ax = plt.subplot(111)
# ax.plot(x, label='$y = numbers')
# # ax.plot(x, y2, label='$y2 = other numbers')
# plt.title('Legend outside')
# chartBox = ax.get_position()
# ax.set_position([chartBox.x0, chartBox.y0, chartBox.width*0.6, chartBox.height])
# ax.legend(loc='upper center', bbox_to_anchor=(1.45, 0.8), shadow=True, ncol=1)
# plt.show()

# y = [2, 4, 6, 8, 10, 12, 14, 16, 18, 20]
# y2 = [10, 11, 12, 13, 14, 15, 16, 17, 18, 19]
# x = np.arange(10)
# fig = plt.figure()
# ax = plt.subplot(111)
# ax.plot(x, y, label='$y = numbers')
# ax.plot(x, y2, label='$y2 = other numbers')
# plt.title('Legend inside')
# ax.legend()
# plt.show()

def get_source_file_name(render_base):
    log_file = open(render_base + '/log', "r")
    return log_file.readline()


def compare(file_1, file_2):
    if filecmp.cmp(file_1, file_2) is False:
        print('Files not equal: {} & {}'.format(file_1, file_2))
        exit(-1)


def check(desktop_base, android_base):
    compare(desktop_base + '/mono', android_base + '/mono')
    compare(desktop_base + '/filtered', android_base + '/filtered')
    # compare(desktop_base + '/magnitude', android_base + '/magnitude')
    # compare(desktop_base + '/event', android_base + '/event')


def run(desktop_base, android_base):

    # check(desktop_base, android_base)
    render_base = desktop_base

    dpi = 80
    plt.figure(figsize=(1600/dpi, 800/dpi), dpi=dpi)
    plt.title(get_source_file_name(render_base))
    plt.xlabel('Time')
    plt.grid(True)
    # ax = plt.subplot(111)
    # chart_box = ax.get_position()
    # ax.set_position([chart_box.x0, chart_box.y0, chart_box.width*0.6, chart_box.height])
    # ax.legend(loc='upper center', bbox_to_anchor=(1.45, 0.8), shadow=True, ncol=1)

    print('Plotting mono')
    mono_signal = read_signal(render_base + '/mono', 1.0)
    print(len(mono_signal))
    plt.plot(mono_signal, label='mono')

    print('Plotting filtered')
    filtered_signal = read_signal(render_base + '/filtered', 0.5)
    print(len(filtered_signal))
    plt.plot(filtered_signal, label='filtered')

    # print('Plotting magnitudes')
    # magnitude_signal = read_average_magnitudes(render_base + '/average_magnitude', len(mono_signal))
    # print(len(magnitude_signal))
    # plt.plot(magnitude_signal, label='magnitude')

    # print('Plotting events')
    # event_plot = read_events(render_base + '/event', "is_note_on", 1, len(mono_signal))
    # print(len(event_plot))
    # plt.plot(event_plot, label='event')

    print('Rendering')
    # plt.tight_layout()
    plt.savefig(render_base + '/graph.png')
    plt.show()


if __name__ == "__main__":
    run(sys.argv[1], sys.argv[2])
