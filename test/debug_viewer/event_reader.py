def new_event(is_note_on, energy, frequency, note, onset_amount):
    return {
        "is_note_on": is_note_on,
        "energy": energy,
        "frequency": frequency,
        "note": note,
        "onset_amount": onset_amount
    }


def convert_events(events_file):

    event_fields = events_file.readlines()

    fields_per_event = 5
    event_count = int(len(event_fields) / fields_per_event)

    events = []

    for i in range(event_count):
        event_offset = (i * fields_per_event)
        is_note_on = int(event_fields[event_offset])
        energy = float(event_fields[event_offset + 1])
        frequency = float(event_fields[event_offset + 2])
        note = float(event_fields[event_offset + 3])
        onset_amount = float(event_fields[event_offset + 4])
        event = new_event(is_note_on, energy, frequency, note, onset_amount)
        events.append(event)

    return events


def plot_events_internal(events, width, attribute, multiplier, size):

    event_offset = int(width / 2)
    signal = [0] * (size + event_offset)

    for i in range(len(events)):
        event = events[i]
        value = event[attribute] * multiplier
        frame_offset = i * width
        signal[frame_offset + event_offset] = value

    return signal


def read_events(path, attribute, multiplier, size):

    events_file = open(path, "r")

    width = int(events_file.readline())
    threshold = float(events_file.readline())
    events = convert_events(events_file)
    print(len(events))
    plot = plot_events_internal(events, width, attribute, multiplier, size)
    return plot
