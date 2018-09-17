package com.ventsell.ventsellorganiser.ticket.objects;

public class Input {
    public static int INPUT_TYPE_SCANNED = 0;
    public static int INPUT_TYPE_COPY_PASTE = 1;
    public static int INPUT_TYPE_ENTERED = 2;

    public int inputType;
    public String ticketId;

    public Input(int inputType, String ticketId) {
        this.inputType = inputType;
        this.ticketId = ticketId;
    }
}

