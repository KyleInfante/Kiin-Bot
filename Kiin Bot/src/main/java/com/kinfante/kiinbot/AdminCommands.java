package com.kinfante.kiinbot;

import de.btobastian.sdcf4j.Command;

public class AdminCommands {

    @Command(aliases = {"!au*"}, description = "Update data for Kiin.")
    public String onAdminUpdateCommand(String command, String[] args)
    {

        return "Data successfully updated!";
    }
}
