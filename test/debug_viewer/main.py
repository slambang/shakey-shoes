import sys
import matplotlib.pyplot as plt
from event_reader import read_events
from signal_reader import read_signal
from average_magnitude_reader import read_average_magnitudes


def get_input_signal_file_path(log_output_base):
    log_file = open(log_output_base + '/log', "r")
    return log_file.readline()


def run(log_output_base):

    fig, ax = plt.subplots()
    plt.title(get_input_signal_file_path(log_output_base))
    plt.xlabel('Time (in samples)')
    plt.grid(True)

    print('Plotting mono')
    mono_signal = read_signal(log_output_base + '/mono', 1.0)
    mono_signal_len = len(mono_signal)
    mono_line, = ax.plot(mono_signal, lw=1, label='Mono Signal (Input)')

    print('Plotting filtered')
    filtered_signal = read_signal(log_output_base + '/filtered', 0.5)
    filtered_line, = ax.plot(filtered_signal, lw=1, label='Filtered Signal')

    print('Plotting average magnitudes')
    magnitude_signal = read_average_magnitudes(log_output_base + '/average_magnitude', mono_signal_len)
    magnitude_line, = ax.plot(magnitude_signal, lw=1, label='Average Magnitude (per window)')

    print('Plotting events')
    event_plot = read_events(log_output_base + '/event', "is_note_on", 1, mono_signal_len)
    events_line, = ax.plot(event_plot, lw=1, label='Onset Events')

    legend = ax.legend(loc='upper right', fancybox=True, shadow=True)
    legend.get_frame().set_alpha(0.4)

    lines = [mono_line, filtered_line, magnitude_line, events_line]
    line_dict = dict()
    for legend_line, data_line in zip(legend.get_lines(), lines):
        legend_line.set_picker(5)  # 5 pts tolerance
        line_dict[legend_line] = data_line

    def on_pick(event):
        # Toggle data visibility
        selected_legend_line = event.artist
        selected_data_line = line_dict[selected_legend_line]
        is_visible = not selected_data_line.get_visible()
        selected_data_line.set_visible(is_visible)

        # Toggle legend alpha
        if is_visible:
            selected_legend_line.set_alpha(1.0)
        else:
            selected_legend_line.set_alpha(0.2)
        fig.canvas.draw()

    fig.canvas.mpl_connect('pick_event', on_pick)

    print('Rendering')
    plt.savefig(log_output_base + '/graph.png')
    plt.show()


if __name__ == "__main__":
    fingerband_debug_log_root = sys.argv[1]
    run(fingerband_debug_log_root)
