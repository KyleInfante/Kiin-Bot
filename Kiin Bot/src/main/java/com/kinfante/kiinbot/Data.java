package com.kinfante.kiinbot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.permissions.Role;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.FileReader;
import java.io.InputStream;
import java.util.*;

public class Data {

    protected static Data _singleton;

    protected JSONArray raidPokemonList;
    protected JSONObject pokemonList;
    protected JSONObject pokeTypesList;
    protected JSONObject pokemonTypeEmojis;
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
        getTypeEmojis();
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
        int index = raidPokemonList.indexOf(name.toLowerCase());

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
        pokeTypesList = getJSON("data\\pokemondata\\types.json");
    }

    private void getTypeEmojis()
    {
        JSONObject jsonObj = getJSON("data\\configs\\appdata.json");
        pokemonTypeEmojis  = (JSONObject)jsonObj.get("icons");
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

    public String getPokemonTypeString(JSONArray arr)
    {
        StringBuilder sb = new StringBuilder("");
        for(int i = 0; i < arr.size(); i++)
        {
            String s = pokemonTypeEmojis.get(arr.get(i)).toString();
            sb.append(s + " ");
        }
        return sb.toString().trim();
    }

    /**
     * Find the weakness types given an array of types.
     * @param arr JSONArray of the pokemon types
     * @return string value of the type icon emojis
     */
    public String getPokemonWeaknessesString(JSONArray arr)
    {
        StringBuilder sb = new StringBuilder("");
        HashMap<String, Integer> typesAndValues = new HashMap<String, Integer>();

        //Loop through each pokemon type
        for(int i = 0; i < arr.size(); i++)
        {
            String typeName = arr.get(i).toString();

            //get the key names under each type
            JSONObject set = (JSONObject)pokeTypesList.get(typeName);
            Set<String> keySet =  set.keySet();

            //Get the values for each key
            Iterator<String> it = keySet.iterator();
            while(it.hasNext())
            {
                String keyName = it.next();
                if(typesAndValues.keySet().contains(keyName))
                {
                    //Add the values together and add the new values to the list
                    int val = Integer.parseInt(typesAndValues.get(keyName).toString());
                    val += Integer.parseInt(set.get(keyName).toString());
                    typesAndValues.replace(keyName, val);
                }
                else
                {
                    int val = Integer.parseInt(set.get(keyName).toString());
                    //Simply add the new  KeyValuePair to the list
                    typesAndValues.put(keyName, val);
                }
            }
        }
        JSONArray resultsArr = new JSONArray();
        Iterator<String> resultKeys = typesAndValues.keySet().iterator();
        while(resultKeys.hasNext()) {
            String key = resultKeys.next();
            if (typesAndValues.get(key) > 0)
                resultsArr.add(key);
        }

        sb.append(getPokemonTypeString(resultsArr));
        return sb.toString().trim();
    }

    public JSONObject getPokemonDataByName(String name)
    {
        JSONObject obj =  (JSONObject)pokemonList.get(name.toLowerCase());
        return obj;
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
