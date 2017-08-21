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
import java.sql.Time;
import java.util.*;

public class Data {

    protected static Data _singleton;

    protected String[] raidPokemonList;
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

    public String FindPokemonName(String name)
    {
        for(int i = 0; i < raidPokemonList.length; i++)
        {
            String pokemon = raidPokemonList[i];
            String pokemonNoSpace = raidPokemonList[i].replace(" ", "");
            if(name.equalsIgnoreCase(pokemon) || name.equalsIgnoreCase(pokemonNoSpace))
            {
                return pokemon;
            }
        }
        return "";
    }

    /**
     * Converts the given time string into hours and minutes, and then adds that time to the current time
     *
     * @param  time string of time value
     * @return time value added to current time.
     */
    public Time getTime(String time)
    {
        Time t = null;
        time = time.replace(":", "");
        time = ("0000" + time).substring(time.length());

        try
        {
            int minutes = Integer.parseInt(time.substring(2, 4));
            int hours = Integer.parseInt(time.substring(0,2));

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, hours);
            cal.add(Calendar.MINUTE, minutes);
            t = new Time(cal.getTime().getTime());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            t = null;
        }

        return t;
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

    /**
     * Read pokemon.json file for the raids pokemon and adds that list to raidPokemonList array
     */
    private void getRaidPokemonData()
    {
        JSONObject jsonObj = getJSON("data\\pokemondata\\pokemon.json");
        JSONArray arr = (JSONArray)jsonObj.get("raids");
        raidPokemonList = new String[arr.size()];

        for(int i = 0 ; i < arr.size(); i++)
        {
            System.out.println(arr.get(i).toString());
            raidPokemonList[i] = arr.get(i).toString();
        }
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
