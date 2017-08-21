package com.kinfante.kiinbot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;

public class AdminCommands implements CommandExecutor {

    @Command(aliases = {"!au*"}, description = "Update data for Kiin.")
    public String onAdminUpdateCommand(String command, String[] args)
    {
        Data._singleton.update();
        return "Data successfully updated!";
    }
}
