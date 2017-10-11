package com.kinfante.kiinbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import java.sql.Time;

public class RaidCommands implements CommandExecutor
{
    private RaidBot rb;

    RaidCommands(RaidBot rb)
    {
        this.rb = rb;
    }

    @Command(aliases = {"!raid", "!r"}, description = "Raid")
    public String onRaidCommand(String[] args, Message msg)
    {
        //make sure raid command has at least 3 arguments
        if(args.length < 3)
            return "Invalid raid command. Try typing your command like this: " +
                    "\"!raid Pokemon Location TimeLeft\"";

        //Delete the user's message if the command was successful.
        msg.delete();

        //Get the Time until the egg/pokemon hatches/despawns
        String time = args[args.length - 1].replace(":", "");
        int length = time.length();
        time = ("0000" + time).substring(length);
        int minutes;
        minutes = -1;
        try
        {
            minutes = Integer.parseInt(time.substring(2, 4));
            int hours = Integer.parseInt(time.substring(0,2));
            minutes += (hours * 60);
        }
        catch(Exception e)
        {
            time = null;
        }

        //Make sure time is in correct format
        if(time == null ||length > 4)
            return "Time is in the wrong format.  Try typing it like this \"HHmm\" or \"HH:mm\"";

        //Time cannot be larger than an hour, since Eggs and Pokemon only last that long.
        if(minutes > 60) return "The time left should never be larger than an hour.";

        //Get hatch/despawn time
        Time exprTime = Data.getExpireTime(minutes);

        //Get Pokemon Name from JSON list
        String pokemonName = Data._singleton.getPokemonName(args);
        if(pokemonName.equalsIgnoreCase("")) return "The Pokemon is not in the raid list. Be sure to check your spelling.";

        //Is it an Egg?
        boolean isEgg = (pokemonName.equalsIgnoreCase("egg3") || pokemonName.equalsIgnoreCase("egg4") || pokemonName.equalsIgnoreCase("egg5"));

        //Get location
        int index = pokemonName.split(" ").length;
        String loc = checkLocation(args, index);

        Raid r;

        //Do different things based on if its an Egg or not.
        if(isEgg)
        {
            //Grab the last digit off of the pokemon name if its an egg (egg3, egg4, egg5)
            String level = pokemonName.substring(pokemonName.length() - 1, pokemonName.length());
            r = new Raid(Integer.parseInt(level));
        }
        else
        {
            r = new Raid(pokemonName);
        }

        r.setLocation(loc);
        r.setExprTime(exprTime);

        //Now build the channel
        BuildRaidChannel(r, minutes);
        return "";
    }

    /**
     * After a raid command has been successful.  This function will be called.
     * Creates the Text Channel in Discord and provides information about the raid and starts the timer.
     * @param raid The raid object that contains the Raid's current state
     */
    private void BuildRaidChannel(Raid raid, int minutes)
    {
        String channelName;
        boolean isEgg = raid.isEgg();
        String location = raid.getLocation();
        String pokemonName = raid.getPokemonName();

        //Change Channel Name based on if it's an egg or not
        if(isEgg)
        {
            channelName = "Egg_LV" + raid.getEggLevel() + "_" + location.replace(" ", "_");
        }
        else
        {
            channelName = pokemonName.replace(" ", "_") + "_" + location.replace(" ", "_");
        }

        Data._singleton.getServer().createChannel(channelName, new FutureCallback<Channel>() {

            @Override
            public void onSuccess(Channel channel)
            {
                channel.updateTopic(raid.getChannelTopic());

                if(isEgg)
                {
                    channel.sendMessage(raid.getRaidStartMessage());
                }
                else
                {
                    raid.sendEmbeddedRaidMessage(channel);
                }

                RaidChannel t = new RaidChannel();
                rb.EnableChannelCommands(t);
                t.setRaidObject(raid);
                t.setTimer(minutes,channel);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private String checkLocation(String[] args, int index)
    {
        String location = args[index++];
        for(int i = index; i < args.length - 1; i++)
        {
            location += " " + args[i];
        }

        char[] tempName = location.toCharArray();
        String retVal = "";
        for(int i = 0 ; i < tempName.length; i++)
        {
            if(Character.isAlphabetic(tempName[i]) || tempName[i] == ' ')
            {
                retVal += tempName[i];
            }
        }

        return retVal;
    }

}