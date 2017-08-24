package com.kinfante.kiinbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
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
                embed.addField("Counter Information", "["+ pokemonName +" Counters](" + counterUrl + ")", false);
                embed.addField("Types", typeString, false);
                //embed.addField("Weaknesses", weaknessesString, true);
                embed.setColor(Color.CYAN);
                //embed.setDescription("This is a description.");
                //embed.setFooter("This is a footer.");
                //embed.setTitle("This is the title.");
                //embed.setUrl("www.google.com");
                /*MessageBuilder message = new MessageBuilder();
                message.append("A " + pokemonName + " raid has been spotted at " + location + "!\n");
                message.append("```css\n");
                message.appendDecoration()
                message.append("Commands for this raid channel:\n");
                message.append(String.format("%-10s %s", "!omw ##", "RSVP to the raid with an optional eta time in minutes.\n"));
                message.append(String.format("%-10s %s", "!otw", "View who has RSVP'd with eta times.\n"));
                message.append(String.format("%-10s %s", "!here", "#ou have arrived at the raid location.  Taking you off of the RSVP list.\n"));
                message.append(String.format("%-10s %s", "!cancel", "Cancel your RSVP.  You are no longer going to attend the raid.```\n"));
                message.append("```\n");
                channel.updateTopic(pokemonName + " raid located at " + location + " until ~" + exprTime);
                channel.sendMessage( message.toString() );*/
                channel.sendMessage("A " + pokemonName + " raid has been spotted at " + location + "!", embed);
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

        return location;
    }
}