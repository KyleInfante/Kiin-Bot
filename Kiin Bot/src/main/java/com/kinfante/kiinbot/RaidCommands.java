package com.kinfante.kiinbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

import java.sql.Time;
import java.util.Calendar;

public class RaidCommands implements CommandExecutor
{
    RaidBot rb;

    public RaidCommands(RaidBot rb)
    {
        this.rb = rb;
    }

    @Command(aliases = {"!raid", "!r"}, description = "Raid")
    public String onRaidCommand(String command, String[] args)
    {
        //make sure raid command has at least 3 arguments
        if(args.length < 3)
            return "Invalid raid command. Try typing your command like this: " +
                    "\"!raid PokemonName Location TimeLeft\"";

        //Get Pokemon Name from list
        String pokemonName = checkPokemonName(args);
        if(pokemonName == "")
            return args[0] + " is not in the raid list. Be sure to check your spelling.";

        String time = args[args.length - 1].replace(":", "");
        int length = time.length();
        time = ("0000" + time).substring(time.length());
        int minutes = Integer.parseInt(time.substring(2, 4));
        int hours = Integer.parseInt(time.substring(0,2));
        minutes += (hours * 60);

        //Get expire time
        Calendar cal = Data._singleton.getTime(minutes);

        if(time == null || length > 4)
            return "Time is in the wrong format.  Try typing it like this \"HHmm\" or \"HH:mm\"";

        //Time cannot be larger than two hours.
        if(minutes > 120) return "The time left should never be larger than 2 hours.";

        Time exprTime = new Time(cal.getTime().getTime());

        //Get location
        int index = pokemonName.split(" ").length;
        String loc = checkLocation(args, index);

        //Now build the channel
        BuildRaidChannel(pokemonName, loc, exprTime, minutes);
        return "Raid Command Worked!";
    }

    private void BuildRaidChannel(String pokemonName, String location, Time exprTime, int minutes)
    {
        String channelName = "raid_" + pokemonName.replace(" ", "_") + "_" + location.replace(" ", "_");
        Data._singleton.server.createChannel(channelName, new FutureCallback<Channel>() {
            @Override
            public void onSuccess(Channel channel)
            {
                MessageBuilder message = new MessageBuilder();
                message.appendRole(Data._singleton.adminRole);
                message.append(" A " + pokemonName + " raid has been spotted at " + location + " until ~" + exprTime);
                channel.updateTopic("A " + pokemonName + " raid has been spotted at " + location + " until ~" + exprTime);
                channel.sendMessage( message.toString() );
                RaidChannel t = new RaidChannel();
                rb.EnableChannelCommands(t);
                t.SetTimer(minutes, channel);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Checking the pokemon name if it exists in the raid list.
     * If it doesn't, it will check if the pokemon is a two worded name
     * @param args the list of arguments in the raid command.
     * @return the pokemon name
     */
    private String checkPokemonName(String[] args)
    {
        //Check for single worded name
        String pokemonName = Data._singleton.FindPokemonName(args[0]);

        //Check if not found, check for two worded name
        if(args.length > 3 && pokemonName == "")
        {
            pokemonName = Data._singleton.FindPokemonName(args[0] + " " + args[1]);
        }

        return pokemonName;
    }

    private String checkLocation(String[] args, int index)
    {
        String location = args[index];
        index++;
        for(int i = index; i < args.length - 1; i++)
            {
            location += " " + args[i];
        }

        return location;
    }
}