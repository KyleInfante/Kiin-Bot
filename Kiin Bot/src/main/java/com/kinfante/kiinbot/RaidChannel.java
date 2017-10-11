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
        private Channel channel;
        private ArrayList<User> onTheWay;
        private ArrayList<Integer> otwEtas;
        private ArrayList<Time> timeOtw;
        private Raid raid;

    public RaidChannel()
    {
        onTheWay = new ArrayList<>();
        otwEtas = new ArrayList<>();
        timeOtw = new ArrayList<>();
    }

    @Command(aliases = {"!coming" , "!c"}, description = "On my way!")
    public String onOmw(String[] args, Channel c, Message msg, User user)
    {
        String omwMsg = "";
        msg.delete();
        if(c == this.channel && !onTheWay.contains(user))
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

        return omwMsg;
    }

    @Command(aliases = {"!who", "!w"}, description = "Who's on the way!?")
    public String onOtw(Channel c, Message msg)
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
    public String onHere(Channel c, Message msg, User user)
    {
        msg.delete();
        String hereMsg = "";
        if(c == this.channel && onTheWay.contains(user))
        {
            int i = onTheWay.indexOf(user);
            if(i == -1) return "";
            onTheWay.remove(i);
            otwEtas.remove(i);
            timeOtw.remove(i);
            hereMsg = user.getName() + " has arrived.";
        }
        return hereMsg;
    }

    @Command(aliases = {"!cancel"}, description = "I will not be able to make it.")
    public String onCancel(Channel c, Message msg, User user)
    {
        msg.delete();
        String cancelMsg = "";
        if(c == this.channel && onTheWay.contains(user))
        {
            int i = onTheWay.indexOf(user);
            if(i == -1) return "";
            onTheWay.remove(i);
            otwEtas.remove(i);
            timeOtw.remove(i);
            cancelMsg = user.getName() + " will no longer be able to make.";
        }
        return cancelMsg;
    }

    @Command(aliases = {"!update", "!u"}, description = "Update Pokemon's name." )
    public String onUpdate(Channel c, Message msg, String[] args, User u)
    {
        String pokemonName;
        String returnMessage;

        if(raid.getPokemonName() != null)
        {
            return "This raid has already been updated with a Pokemon Name.";
        }

        //Lets make sure it isn't an egg again.
        if(args[0].equalsIgnoreCase("egg3") || args[0].equalsIgnoreCase("egg4") || args[0].equalsIgnoreCase("egg5"))
        {
            return "Uh, this pokemon can't be an egg again...";
        }

        if(c == this.channel && (args.length == 1 || args.length == 2))
        {
            pokemonName = Data._singleton.getPokemonName(args);

            if(!pokemonName.equalsIgnoreCase(""))
            {
                raid.setAwaitingUpdate(false);
                raid.setPokemonName(pokemonName);
                channel.update(raid.getChannelName(),
                        raid.getChannelTopic(),
                        channel.getPosition()
                );
                raid.sendEmbeddedRaidMessage(channel);
                returnMessage = u.getName() + " has updated this Pokemon's name to " + pokemonName;
            }
            else
            {
                returnMessage = "The Pokemon name provided is invalid.  Please check your spelling.";
            }
        }
        else
        {
            returnMessage = "This command is invalid. Try typing it like this, \"!update pokemonName\"";
        }

        msg.delete();

        return returnMessage;
    }

    /**
     * Sets the exiration timer for this channel to delete
     * @param minutes number of minutes this channel will remain up
     * @param channel this channel
     */
    public void setTimer(int minutes, Channel channel)
    {
        this.channel = channel;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
        if(!raid.isEgg())
        {
            scheduler.schedule(() -> { DeleteChannel(); return null; }, minutes, TimeUnit.MINUTES);
        }
        else
        {
            scheduler.schedule(() -> { EggToPokemonChannel(); return null; }, minutes, TimeUnit.MINUTES);
        }
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

    private void EggToPokemonChannel()
    {
        raid.setIsEgg(false);
        raid.setAwaitingUpdate(true);
        raid.setExprTime(Data.getExpireTime(60));

        channel.update(
                raid.getChannelName(),
                raid.getChannelTopic(),
                channel.getPosition()
        );
        channel.sendMessage(raid.getHatchedMessage());
        setTimer(60, channel);
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

    public void setRaidObject(Raid raidObject) {
        this.raid = raidObject;
    }
}
