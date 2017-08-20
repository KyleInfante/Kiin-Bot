package com.kinfante.kiinbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import java.time.LocalDateTime;

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
                    "\"!raid <PokemonName> <Location> <TimeLeft>\"";

        String pokemonName = Data._singleton.FindPokemonName(args[0]);
        if(pokemonName == "")
        {
            return args[0] + " is not in the raid list. Be sure to check your spelling.";
        }

        BuildRaidChannel(pokemonName,args[1], LocalDateTime.now());
        return "Raid Command Worked!";
    }

    private void BuildRaidChannel(String pokemon, String location, LocalDateTime dt)
    {

        Data._singleton.server.createChannel("RAID_" + pokemon + "_" + location, new FutureCallback<Channel>() {
            @Override
            public void onSuccess(Channel channel)
            {
                MessageBuilder message = new MessageBuilder();
                message.appendRole(Data._singleton.adminRole);
                message.append("A " + pokemon + " raid has popped up at " + location);
                channel.sendMessage( message.toString() );
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }
}