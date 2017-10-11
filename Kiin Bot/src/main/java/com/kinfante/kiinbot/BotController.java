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
        //Initialize the Data object
        Data data = new Data();
        data.initApi();
        api = data.getApi();

        //Data singleton has been generated here, so continue to use the singleton to reference the current Data object
    }

    /**
     * Called from KiinGo main function
     */
    public void StartRaidBot()
    {
        RaidBot raidBot = new RaidBot(api);
        raidBot.Start();
    }
}
