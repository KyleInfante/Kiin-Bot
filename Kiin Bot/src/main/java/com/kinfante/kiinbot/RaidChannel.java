package com.kinfante.kiinbot;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RaidChannel implements CommandExecutor
{
        Channel channel;
        ArrayList<User> onTheWay;
        ArrayList<Integer> otwEtas;
        ArrayList<Time> timeOtw;

    public RaidChannel()
    {
        onTheWay = new ArrayList<>();
        otwEtas = new ArrayList<>();
        timeOtw = new ArrayList<>();
    }

    @Command(aliases = {"!omw"}, description = "On my way!")
    public String onOmw(String command, String[] args, User user, Channel c, Message msg)
    {
        String omwMsg = "";
        if(c == this.channel && !IsOTW(user))
        {
            omwMsg = user.getName() + " is on the way.  ";
            if(args.length > 0 && args[0].length() <= 2)
            {
                try {
                    int minutes = Integer.parseInt(args[0]);
                    otwEtas.add(minutes);
                    omwMsg += "ETA: " + minutes + " minutes!";
                }
                catch (Exception e)
                { }
            }
            else
            {
                otwEtas.add(null);
            }

            onTheWay.add(user);
            Time t = new Time(Calendar.getInstance().getTime().getTime());
            timeOtw.add(t);
        }
        msg.delete();
        return omwMsg;
    }

    @Command(aliases = {"!otw"}, description = "Who's on the way!?")
    public String onOtw(String command, String[] args, Channel c, Message msg)
    {

        MessageBuilder mb = new MessageBuilder();
        mb.append("```\n");
        if(c == this.channel)
        {
            String otwMsg = "There are " + onTheWay.size() + " trainers on the way!\n--------------------------------\n";
            if(onTheWay.size() == 1)
                otwMsg = "There is " + onTheWay.size() + " trainer on the way!\n--------------------------------\n";
            else if(onTheWay.size() == 0)
                otwMsg = "There are no trainers on the way!\n";
            mb.append(otwMsg);
            UpdateOtwTimes();

            for(int i = 0 ;  i < onTheWay.size(); i++)
            {
                mb.append(onTheWay.get(i).getName());
                if(otwEtas.get(i) != null)
                {
                    int eta = otwEtas.get(i);
                    switch(eta)
                    {
                        case(0): {
                            mb.append(" should be close.\n");
                            break;
                        }
                        case(1): {
                            mb.append(" in " + eta + " minute.\n");
                            break;
                        }
                        default: {
                            mb.append(" in " + eta + " minutes.\n");
                            break;
                        }
                    }
                }
                else
                {
                    mb.append("\n");
                }
            }
        }
        mb.append("```");
        msg.delete();
        return mb.toString();
    }

    @Command(aliases = {"!here", "!h"}, description = "I am here at the raid!")
    public String onHere(String command, String[] args, Channel c, Message msg, User user)
    {
        msg.delete();
        String hereMsg = "";
        if(c == this.channel && onTheWay.contains(user))
        {
            int i = getOtwIndex(user);
            if(i == -1) return "";
            onTheWay.remove(i);
            otwEtas.remove(i);
            timeOtw.remove(i);
            hereMsg = user.getName() + " has arrived.";
        }
        return hereMsg;
    }

    @Command(aliases = {"!cancel", "!c"}, description = "I will not be able to make it.")
    public String onCancel(String command, String[] args, Channel c, Message msg, User user)
    {
        msg.delete();
        String cancelMsg = "";
        if(c == this.channel && onTheWay.contains(user))
        {
            int i = getOtwIndex(user);
            if(i == -1) return "";
            onTheWay.remove(i);
            otwEtas.remove(i);
            timeOtw.remove(i);
            cancelMsg = user.getName() + " is no longer on the way";
        }
        return cancelMsg;
    }

    /**
     * Sets the exiration timer for this channel to delete
     * @param minutes number of minutes this channel will remain up
     * @param channel this channel
     */
    public void SetTimer(int minutes, Channel channel)
    {
        this.channel = channel;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
        scheduler.schedule(() -> { DeleteChannel(); return null; }, minutes, TimeUnit.MINUTES);
    }

    /**
     * Runs after the timer thread has hit the number of minutes given.
     */
    private void DeleteChannel()
    {
        try
        {
            channel.delete();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private int getOtwIndex(User user)
    {
        for(int i = 0; i < onTheWay.size(); i++)
        {
            if(onTheWay.get(i) == user)
                return i;
        }
        return -1;
    }

    private boolean IsOTW(User user)
    {
        for(User u : onTheWay)
        {
            if(u == user) return true;
        }

        return false;
    }

    private void UpdateOtwTimes()
    {
        for(int i = 0; i < otwEtas.size(); i++)
        {
            if(otwEtas.get(i) != null && otwEtas.get(i) != 0)
            {
                //Get otwTime
                Time then = timeOtw.get(i);

                //Get now
                Time now = new Time(Calendar.getInstance().getTime().getTime());

                //Subtract the number of minutes from timeOtw and now
                long timeDiff = now.getTime() -  then.getTime();
                long minuteDiff = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
                int newMinutes = (int)(otwEtas.get(i) - minuteDiff);

                if(newMinutes < 0) newMinutes = 0;
                otwEtas.set(i, newMinutes);
            }
        }
    }
}
