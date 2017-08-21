package com.kinfante.kiinbot;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RaidChannel implements CommandExecutor
{
        Channel channel;
        ArrayList<User> onTheWay;
        ArrayList<Integer> otwEtas;

    public RaidChannel()
    {
        onTheWay = new ArrayList<>();
        otwEtas = new ArrayList<>();
    }

    @Command(aliases = {"!omw"}, description = "On my way!")
    public String onOmw(String command, String[] args, User user, Channel c)
    {
        if(c == this.channel && !IsOTW(user))
        {
            if(args.length > 0)
                try
                {
                    int minutes = Integer.parseInt(args[0]);
                    otwEtas.add(minutes);
                }
                catch(Exception e)
                {
                    return "ETA time was in the wrong format.";
                }
            else
            {
                otwEtas.add(null);
            }

            onTheWay.add(user);
        }
        return null;
    }

    @Command(aliases = {"!otw"}, description = "Who's on the way!?")
    public String onOtw(String command, String[] args, Channel c)
    {
        StringBuilder sb = new StringBuilder("");
        if(c == this.channel)
        {
            for(int i = 0 ;  i < onTheWay.size(); i++)
            {
                sb.append(onTheWay.get(i).getName());
                if(otwEtas.get(i) != null)
                    sb.append(" in " + otwEtas.get(i).toString() + " minutes.\n");
                else
                    sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void SetTimer(int minutes, Channel channel)
    {
        this.channel = channel;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
        scheduler.schedule(() -> { DeleteChannel(); return null; }, minutes, TimeUnit.MINUTES);
    }

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

    private boolean IsOTW(User user)
    {
        for(User u : onTheWay)
        {
            if(u == user) return true;
        }

        return false;
    }
}
