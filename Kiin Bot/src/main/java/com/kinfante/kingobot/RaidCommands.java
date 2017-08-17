package com.kinfante.kingobot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;

public class RaidCommands implements CommandExecutor
{
    @Command(aliases = {"!raid", "!r"}, description = "Raid")
    public String onRaidCommand(String command, String[] args)
    {
        String retVal;

        if(args.length != 3)
            return "Invalid raid command. Try typing your command like this " +
                    "\"!raid <PokemonName> <Location> <TimeLeft(00:00)>\"";

        BuildRaidChannel(args[0],args[1], LocalDateTime.now());
        return "Raid Command Worked!";
    }

    private void BuildRaidChannel(String pokemon, String location, LocalDateTime dt)
    {
        Collection<Server> coll = BotController.API.getServers();
        Server[] servers = coll.toArray(new Server[coll.size()]);
        Server server = servers[0];
        server.createChannel("RAID: " + pokemon + " at " + location, new FutureCallback<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                channel.sendMessage("@Admin A " + pokemon + " raid has popped up at " + location);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }
}