package com.kinfante.kingobot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.sdcf4j.*;
import de.btobastian.sdcf4j.handler.JavacordHandler;

public class RaidBot {

    /***
     * Enables the Raid Bot and puts it online.
     */
    public void Start()
    {
        BotController.API.connect(new FutureCallback<DiscordAPI>() {
            public void onSuccess(DiscordAPI discordAPI) {
                System.out.println("Kiin is online!");
                /*
                discordAPI.registerListener(new MessageCreateListener() {
                    public void onMessageCreate(DiscordAPI discordAPI, Message message) {
                        if(message.getContent().equalsIgnoreCase("ping"))
                        {
                            message.reply("pong");
                        }
                    }
                });*/
            }

            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        EnableCommands();
    }

    private void EnableCommands()
    {
        if(BotController.RAID_COMMANDS_ON)
        {
            CommandHandler cmdHandler = new JavacordHandler(BotController.API);
            cmdHandler.registerCommand(new RaidCommands());
        }
    }
}
