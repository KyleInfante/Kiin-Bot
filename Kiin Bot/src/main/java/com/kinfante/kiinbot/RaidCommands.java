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
        if(args.length != 3)
            return "Invalid raid command. Try typing your command like this: " +
                    "\"!raid PokemonName Location TimeLeft\"";

        String pokemonName = Data._singleton.FindPokemonName(args[0]);
        if(pokemonName == "")
        {
            return args[0] + " is not in the raid list. Be sure to check your spelling.";
        }

        Time time = Data._singleton.getTime(args[2]);

        if(args[2].replace(":", "").length() > 5 || time == null)
            return "Time is in the wrong format.  Try typing it like this \"HHmm\" or \"HH:mm\"";

        BuildRaidChannel(pokemonName,args[1], time);
        return "Raid Command Worked!";
    }

    private void BuildRaidChannel(String pokemon, String location, Time time)
    {

        Data._singleton.server.createChannel("raid_" + pokemon + "_" + location, new FutureCallback<Channel>() {
            @Override
            public void onSuccess(Channel channel)
            {
                MessageBuilder message = new MessageBuilder();
                message.appendRole(Data._singleton.adminRole);
                message.append("A " + pokemon + " raid has popped up at " + location);
                channel.updateTopic(pokemon + " raid located at " + location + ". Channel expires at " + time);
                channel.sendMessage( message.toString() );
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }
}