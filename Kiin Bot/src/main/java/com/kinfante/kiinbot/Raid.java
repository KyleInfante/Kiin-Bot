package com.kinfante.kiinbot;
import com.google.common.util.concurrent.FutureCallback;
import com.sun.istack.internal.Nullable;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageBuilder;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.sql.Time;

public class Raid {

    private final String TO = "";

    private String pokemonName;
    private int eggLevel;
    private boolean isEgg;
    private String location;
    private Time hatchTime;
    private Time despawnTime;
    private EmbedBuilder embed;
    private boolean awaitingUpdate = false;

    /**
     * Constructor when its an Egg
     * @param eggLevel
     */
    public Raid(int eggLevel)
    {
        this.isEgg = true;
        this.eggLevel = eggLevel;
    }

    /**
     * Constructor when its hatched from egg
     * @param pokemonName
     */
    public Raid(String pokemonName)
    {
        this.isEgg = false;
        this.pokemonName = pokemonName;
    }

    /**
     * Get the Channel's topic, a quick summary of the channel and whats going on with the raid
     * @return the string topic value
     */
    public String getChannelTopic()
    {
        String topic;
        if(isEgg)
        {
            topic = "Level " + eggLevel + " egg located at " + location + ", hatching at ~" + hatchTime;

        }
        else
        {
            if(awaitingUpdate)
            {
                topic = "A level " + eggLevel + " egg has hatched at " + location + ", but we don't know which Pokemon it is.\n" +
                        "Type \"!update pokemonName\" to update the pokemon's name.";
            }
            else
            {
                topic = pokemonName + " raid located at " + location + " until ~" + despawnTime;
            }

        }
        return topic;
    }

    public String getChannelName()
    {
        String channelName;

        if(isEgg)
        {
            channelName = "Egg_LV" + eggLevel + "_" + location.replace(" ", "_");
        }
        else
        {
            if(awaitingUpdate)
            {
                channelName = "level_" + eggLevel + "_" + location.replace(" ", "_");
            }
            else
            {
                channelName = pokemonName.replace(" ", "_") + "_" + location.replace(" ", "_");
            }
        }
        return channelName;
    }

    public String getRaidStartMessage()
    {
        String message;
        if(isEgg)
        {
            message = TO + "A level " + eggLevel + " egg located at " + location + " will be hatching at ~" + hatchTime;
        }
        else
        {
            char first = pokemonName.toLowerCase().charAt(0);
            if(first == 'a' || first == 'e' || first == 'i' || first == 'o' || first == 'u')
            {
                message = TO + "An " + pokemonName + " raid has been spotted at " + location + "!";
            }
            else
            {
                message = TO + "A " + pokemonName + " raid has been spotted at " + location + "!";
            }

        }
        return message;
    }

    public String getHatchedMessage()
    {
        return  TO + "A level " + eggLevel + " egg has hatched at " + location + ", but we don't know which Pokemon it is.\n" +
                "Type \"!update pokemonName\" to update the pokemon's name.\n" +
                "This pokemon despawns at ~" + despawnTime;
    }

    public void setAwaitingUpdate(boolean b)
    {
        awaitingUpdate = b;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public void setPokemonName(String pokemonName) {
        this.pokemonName = pokemonName;
    }

    public int getEggLevel() {
        return eggLevel;
    }

    public boolean isEgg() {
        return isEgg;
    }

    public void setIsEgg(boolean egg) {
        isEgg = egg;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setExprTime(Time time) {
        if(isEgg) {
            this.hatchTime = time; }
        else {
            this.despawnTime = time; }
    }

    public EmbedBuilder getEmbed() {

        if(embed == null)
            buildEmbed();

        return embed;
    }

    private void buildEmbed()
    {
        String raidCountersPath = "https://pokemongo.gamepress.gg/pokemon/";
        String raidCountersPathEnd = "#raid-boss-counters";
        String pokemonThumbnail= "https://poketoolset.com/assets/img/pokemon/images/";

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
        this.embed = embed;
    }

    void sendEmbeddedRaidMessage(Channel c)
    {
        EmbedBuilder embed = getEmbed();
        c.sendMessage(getRaidStartMessage(), embed, new FutureCallback<Message>() {
            @Override
            public void onSuccess(Message message) {
                MessageBuilder m = new MessageBuilder();
                m.append("```\n");
                m.append("Raid Channel Commands\n");
                m.append(String.format("%-10s %s", "!coming ##", "RSVP to the raid with optional ETA time.\n"));
                m.append(String.format("%-10s %s", "!who", "View who is on the way.\n"));
                m.append(String.format("%-10s %s", "!here", "You have arrived at the raid. Taking you off of the RSVP list.\n"));
                m.append(String.format("%-10s %s", "!cancel", "Cancel your RSVP.\n"));
                m.append("```\n");

                c.sendMessage(m.toString());
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        } );
    }
}
