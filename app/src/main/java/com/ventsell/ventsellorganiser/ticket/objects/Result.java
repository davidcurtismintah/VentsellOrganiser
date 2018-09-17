package com.ventsell.ventsellorganiser.ticket.objects;

public class Result {

    public static int RESULT_TYPE_VALID = 0;
    public static int RESULT_TYPE_NOT_VALID = 1;

    public long timestamp;
    public int resultType;

    public Input input;

    public Result(Input input, int resultType) {
        this.input = input;
        this.resultType = resultType;
    }

    public Result(int inputType, String ticketId, long timestamp, int resultType) {
        this.input = new Input(inputType, ticketId);
        this.timestamp = timestamp;
        this.resultType = resultType;
    }

    public String getDisplayContents() {
        switch (resultType){
            case 0:
                return "* valid *";
            case 1:
                return "not valid";
            default:
                return "";
        }
    }
}

