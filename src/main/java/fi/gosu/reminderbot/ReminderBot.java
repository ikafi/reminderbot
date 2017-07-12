package fi.gosu.reminderbot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

import java.io.IOException;
import java.util.*;

/**
 * @author Ville Nupponen
 * @since .
 */
public class ReminderBot extends PircBot {

    public static final List<Reminder> reminders = new ArrayList<Reminder>();
    public static final ReminderBot reminderBot = new ReminderBot();
    public static int lastDay = -1;

    public static void main(String[] args) throws IOException, IrcException {
        reminderBot.setName("ReminderBot");
        reminderBot.setVersion("ReminderBot v. 0.1.1");
        reminderBot.setLogin("reminderbot");
        reminderBot.setVerbose(true);
        reminderBot.connect("irc.quakenet.org");
        reminderBot.joinChannel("#eka.priva", "farmitulisee");
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                if (lastDay != calendar.get(Calendar.DATE)) {
                    for (Reminder reminder : reminders) {
                        reminder.setDone(false);
                    }
                    lastDay = calendar.get(Calendar.DATE);
                }
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                for (Reminder reminder : reminders) {
                    if (!reminder.isDone() && hours >= reminder.getHours() && minutes >= reminder.getMinutes()) {
                        reminderBot.sendMessage("#eka.priva", reminder.getText());
                        reminder.setDone(true);
                    }
                }
            }
        },0,5000);
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        if (hostname.equals("karkkimies.users.quakenet.org") || hostname.equals("Pekka1.users.quakenet.org")) {
            String[] parts = message.split(" ");
            if (parts.length > 0) {
                String command = parts[0];
                if (command.equals("!add") && parts.length >= 3) {
                    String time = parts[1];
                    String text = message.substring(message.indexOf(" ", 5) + 1);
                    Reminder reminder = new Reminder(text, time);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    int hours = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = calendar.get(Calendar.MINUTE);
                    if (hours > reminder.getHours() || (hours == reminder.getHours() && minutes > reminder.getMinutes())) {
                        reminder.setDone(true);
                    }
                    reminders.add(reminder);
                } else if (command.equals("!list")) {
                    for (int i = 0; i < reminders.size(); i++) {
                        reminderBot.sendMessage("#eka.priva", (i+1) + ") " + reminders.get(i).getTime() + " - " + reminders.get(i).getText());
                    }
                } else if (command.equals("!delete") && parts.length == 2) {
                    reminders.remove(Integer.parseInt(parts[1]) - 1);
                }
            }
        }
    }

    private class Reminder {
        private String text;
        private String time;
        private boolean done;

        public Reminder(String text, String time) {
            this.text = text;
            this.time = time;
            this.done = false;
        }

        public String getText() {
            return text;
        }
        public String getTime() {
            return time;
        }
        public int getHours() {
            return Integer.parseInt(time.split(":")[0]);
        }
        public int getMinutes() { return Integer.parseInt(time.split(":")[1]); }
        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }
    }
}
