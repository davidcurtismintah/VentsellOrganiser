package com.ventsell.ventsellorganiser.ticket.objects;

public class VentsellETicketObject {

    VentsellETicket success;

    @Override
    public String toString() {
        return "VentsellETicketObject{" +
                "success=" + success +
                '}';
    }

    public static class VentsellETicket {
        String text;

        @Override
        public String toString() {
            return "VentsellETicket{" +
                    "ticketId='" + text + '\'' +
                    '}';
        }
    }
}
