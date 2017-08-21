package com.kinfante.kiinbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class RaidCommands implements CommandExecutor
{

    public RaidCommands()
    {
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


        //Get time
        Time time = Data._singleton.getTime(args[args.length - 1]);
        if(args[args.length - 1].replace(":", "").length() > 4 || time == null)
            return "Time is in the wrong format.  Try typing it like this \"HHmm\" or \"HH:mm\"";

        //Get location
        int index = pokemonName.split(" ").length;
        String loc = checkLocation(args, index);

        //Now build the channel
        BuildRaidChannel(pokemonName, loc, time);
        return "Raid Command Worked!";
    }

    private void BuildRaidChannel(String pokemonName, String location, Time exprTime)
    {
        Data._singleton.server.createChannel("raid_" + pokemonName.replace(" ", "_") + "_" +
                location.replace(" ", "_"), new FutureCallback<Channel>() {
            @Override
            public void onSuccess(Channel channel)
            {
                MessageBuilder message = new MessageBuilder();
                message.appendRole(Data._singleton.adminRole);
                message.append("A " + pokemonName + " raid has popped up at " + location);
                channel.updateTopic(pokemonName + " raid located at " + location + " until ~" + exprTime);
                channel.sendMessage( message.toString() );
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