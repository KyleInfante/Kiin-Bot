package com.kinfante.kingobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Scanner;

public class BotController
{
    private String botToken;

    protected static final boolean RAID_COMMANDS_ON = true;

    protected static DiscordAPI API;

    public BotController()
    {
        getBotToken();
        API = Javacord.getApi(botToken, true);
        API.setGame("Pokemon GO");
    }

    public void StartRaidBot()
    {
        RaidBot raidBot = new RaidBot();
        raidBot.Start();
    }

    //region Configuration functions
    /**
     * Gets and Sets the botToken from the configs.json file located in the resources folder.
     */
    private void getBotToken()
    {
        ClassLoader cl = getClass().getClassLoader();
        InputStream stream = cl.getResourceAsStream("configs/config.json");
        String fileContents = getFileContents(stream);
        JSONObject jsonObj = new JSONObject(fileContents);
        this.botToken = jsonObj.get("bot-token").toString();
    }

    /***
     * Read given input stream
     * @param stream the given input stream
     * @return all of the contents of the file as a String
     */
    private String getFileContents(InputStream stream)
    {
        StringBuilder contents = new StringBuilder("");

        try
        {
            Scanner scanner = new Scanner(stream);

            while(scanner.hasNextLine())
            {
                contents.append(scanner.nextLine());
            }
            scanner.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return contents.toString();
    }
    //endregion
}
