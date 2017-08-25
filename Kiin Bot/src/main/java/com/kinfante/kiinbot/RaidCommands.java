package com.kinfante.kiinbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.sql.Time;
import java.util.Calendar;

public class RaidCommands implements CommandExecutor
{
    RaidBot rb;

    String raidCountersPath = "https://pokemongo.gamepress.gg/pokemon/";
    String raidCountersPathEnd = "#raid-boss-counters";
    String pokemonThumbnail= "https://poketoolset.com/assets/img/pokemon/images/";

    public RaidCommands(RaidBot rb)
    {
        this.rb = rb;
    }

    @Command(aliases = {"!raid", "!r"}, description = "Raid")
    public String onRaidCommand(String command, String[] args, Message msg)
    {
        msg.delete();

        //make sure raid command has at least 3 arguments
        if(args.length < 3)
            return "Invalid raid command. Try typing your command like this: " +
                    "\"!raid PokemonName Location TimeLeft\"";

        //Get Pokemon Name from list
        String pokemonName = checkPokemonName(args);
        if(pokemonName == "")
            return "The Pokemon is not in the raid list. Be sure to check your spelling.";

        String time = args[args.length - 1].replace(":", "");
        int length = time.length();
        time = ("0000" + time).substring(length);
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
        return "";
    }

    private void BuildRaidChannel(String pokemonName, String location, Time exprTime, int minutes)
    {
        String channelName = "raid_" + pokemonName.replace(" ", "_") + "_" + location.replace(" ", "_");
        Data._singleton.server.createChannel(channelName, new FutureCallback<Channel>() {
            @Override
            public void onSuccess(Channel channel)
            {
                JSONObject jsonObj = Data._singleton.getPokemonDataByName(pokemonName);
                int id = Integer.parseInt(jsonObj.get("id").toString());
                JSONArray typesArray = (JSONArray)jsonObj.get("types");
                String counterUrl = raidCountersPath + id + raidCountersPathEnd;
                String thumbnailUrl = pokemonThumbnail + id + ".png";
                String typeString = Data._singleton.getPokemonTypeString(typesArray);
                String weaknessesString = Data._singleton.getPokemonWeaknessesString(typesArray);

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(pokemonName + " Raid Information");
                embed.setThumbnail(thumbnailUrl);
                embed.addField("Types", typeString, true);
                embed.addField("Weaknesses", weaknessesString, true);
                embed.setColor(Color.CYAN);
                embed.setDescription("["+ pokemonName +" Counters](" + counterUrl + ")");
                channel.updateTopic(pokemonName + " raid located at " + location + " until ~" + exprTime);
                channel.sendMessage("@everyone A " + pokemonName + " raid has been spotted at " + location + "!", embed, new FutureCallback<Message>() {
                    @Override
                    public void onSuccess(Message message) {
                        MessageBuilder m = new MessageBuilder();
                        m.append("```\n");
                        m.append("Raid Channel Commands\n");
                        m.append(String.format("%-10s %s", "!omw ##", "RSVP to the raid with optional ETA time.\n"));
                        m.append(String.format("%-10s %s", "!otw", "View who is on the way.\n"));
                        m.append(String.format("%-10s %s", "!here", "You have arrived at the raid. Taking you off of the RSVP list.\n"));
                        m.append(String.format("%-10s %s", "!cancel", "Cancel your RSVP.\n"));
                        m.append("```\n");

                        channel.sendMessage(m.toString());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

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
        String pokemonName = Data._singleton.FindRaidPokemonName(args[0]);

        //Check if not found, check for two worded name
        if(args.length > 3 && pokemonName == "")
        {
            pokemonName = Data._singleton.FindRaidPokemonName(args[0] + " " + args[1]);
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