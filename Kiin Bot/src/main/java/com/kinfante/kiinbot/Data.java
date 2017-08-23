package com.kinfante.kiinbot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.permissions.Role;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileReader;
import java.io.InputStream;
import java.util.*;

public class Data {

    protected static Data _singleton;

    protected JSONArray raidPokemonList;
    protected JSONObject pokemonList;
    protected JSONObject pokeTypesList;
    protected Role adminRole;
    protected Role instinctRole;
    protected Role mysticRole;
    protected Role valorRole;
    protected Server server;
    private DiscordAPI api;
    private String botToken;

    public Data()
    {
        if(_singleton == null)
        {
            _singleton = this;
        }
    }

    /**
     * Runs on the start of the bot
     */
    public void init()
    {
        //Get the server object
        Collection<Server> collS = api.getServers();
        Server[] servers = collS.toArray(new Server[collS.size()]);
        server = servers[0];

        getRoleData();
        getTypesData();
        update();
    }

    /**
     * Initialized the api object.
     * Gets the bot token from the auth.json file and authenticates to discord with it.
     */
    public void initApi()
    {
        JSONObject jsonObj = getJSON("data\\configs\\auth.json");
        botToken = jsonObj.get("bot-token").toString();
        api = Javacord.getApi(botToken, true);
        api.setGame("Pokemon GO");
    }

    public DiscordAPI getApi()
    {
        return api;
    }

    /**
     * Parse the pokemon.json and types.json
     */
    public void update()
    {
        getRaidPokemonData();
    }

    public String FindRaidPokemonName(String name)
    {
        String pName = "";
        int index = raidPokemonList.indexOf(name);
        System.out.println(index);
        if(index != -1)
        {
            pName = raidPokemonList.get(index).toString();
            String[] splits = pName.trim().split(" ");
            if(splits.length > 1)
            {
                String first = splits[0].substring(0,1).toUpperCase() + splits[0].substring(1);
                String second = splits[1].substring(0,1).toUpperCase() + splits[1].substring(1);
                pName = first + " " + second;
            }
            else
            {
                pName = pName.substring(0,1).toUpperCase() + pName.substring(1);
            }
        }
        System.out.println(pName);
        return pName;
    }

    /**
     * Converts the given time string into hours and minutes, and then adds that time to the current time
     *
     * @param minutes number of minutes raid will remain up
     * @return time value added to current time.
     */
    public Calendar getTime(int minutes)
    {
        Calendar cal = Calendar.getInstance();

        try
        {
            cal.add(Calendar.MINUTE, minutes);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            cal = null;
        }

        return cal;
    }

    //region  Data Retrieval from JSON files.
    private void getRoleData()
    {
        JSONObject jsonObj = getJSON("data\\configs\\appdata.json");
        jsonObj = (JSONObject)jsonObj.get("role_names");

        //Role objects
        Collection<Role> collR = server.getRoles();
        Role[] roles = collR.toArray(new Role[collR.size()]);

        for(int i = 0; i < roles.length; i++)
        {
            String roleName = roles[i].getName();

            if(roleName.equalsIgnoreCase(jsonObj.get("admin").toString()))
            {
                adminRole = roles[i];
                continue;
            }

            if(roleName.equalsIgnoreCase(jsonObj.get("instinct").toString().toLowerCase()))
            {
                instinctRole = roles[i];
                continue;
            }

            if(roleName.equalsIgnoreCase(jsonObj.get("valor").toString().toLowerCase()))
            {
                valorRole = roles[i];
                continue;
            }

            if(roleName.equalsIgnoreCase(jsonObj.get("mystic").toString().toLowerCase()))
            {
                mysticRole = roles[i];
                continue;
            }
        }
    }

    private void getTypesData()
    {
        JSONObject jsonObj = getJSON("data\\pokemondata\\types.json");
        pokeTypesList = (JSONObject)jsonObj.get("types");
    }

    /**
     * Read pokemon.json file for the raids pokemon and adds that list to raidPokemonList array
     */
    private void getRaidPokemonData()
    {
        JSONObject jsonObj = getJSON("data\\pokemondata\\pokemon.json");
        raidPokemonList = (JSONArray)jsonObj.get("raids");
        pokemonList = (JSONObject)jsonObj.get("pokemon_list");
    }

    /**
     * Grabs configuration data like API Tokens and sensitive information from auth.json file
     * @return JSONObject of the json file.
     */
    protected JSONObject getJSON(String path)
    {
        /*ClassLoader cl = getClass().getClassLoader();
        InputStream stream = cl.getResourceAsStream(path);*/
        JSONObject jsonObj = null;
        try
        {
            JSONParser parser = new JSONParser();
            jsonObj = (JSONObject)parser.parse(new FileReader(path));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return jsonObj;
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
