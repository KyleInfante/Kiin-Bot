package com.kinfante.kiinbot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.permissions.Role;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Scanner;

public class BotController
{
    private DiscordAPI api;

    public BotController()
    {
        //Stores the data into this object
        Data data = new Data();
        data.initApi();
        api = data.getApi();
    }

    public void StartRaidBot()
    {
        RaidBot raidBot = new RaidBot(api);
        raidBot.Start();
    }
}
