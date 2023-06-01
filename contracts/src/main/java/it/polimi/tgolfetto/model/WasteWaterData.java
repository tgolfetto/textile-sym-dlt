package it.polimi.tgolfetto.model;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WasteWaterData implements Serializable {
    /*
    Sustainability Macro Categories (SMCs) contained into the Sustainability Data Matrix developed by UNECE:
    - Hazardous chemicals (SMC 1)
    - Water consumption and pollution (SMC 2)
    - GHG emissions and air pollution (SMC 3)
    - Energy consumption and efficiency (SMC 4)
    - Solid waste (SMC 5)
     */

    private final SMC SMC1_cod; //Chemical Oxygen Demand
    private final SMC SMC1_bod; //Biological Oxygen Demand
    private final SMC SMC1_chlorides; //Chlorides
    private final SMC SMC1_tkn; //Total Kjeldahl Nitrogen
    private final SMC SMC1_sulphates; //Sulphates
    private final SMC SMC1_zink; //Zink
    private final SMC SMC1_copper; //Copper
    private final SMC SMC1_arsenic; //Arsenic
    private final SMC SMC1_sodium; //Sodium
    private final SMC SMC2_ph; //PH of wastewater
    private final SMC SMC2_temperature; //Temperature of wastewater
    private final SMC SMC5_oilAndGrease; //Oil and Grease
    private final SMC SMC5_tds; //Total dissolved solids
    private final HashMap<String, SMC> hashMap;

    public WasteWaterData(SMC SMC1_cod, SMC SMC1_bod, SMC SMC1_chlorides, SMC SMC1_tkn, SMC SMC1_sulphates, SMC SMC1_zink, SMC SMC1_copper, SMC SMC1_arsenic, SMC SMC1_sodium, SMC SMC2_ph, SMC SMC2_temperature, SMC SMC5_oilAndGrease, SMC SMC5_tds) {
        this.SMC1_cod = SMC1_cod;
        this.SMC1_bod = SMC1_bod;
        this.SMC2_temperature = SMC2_temperature;
        this.SMC1_chlorides = SMC1_chlorides;
        this.SMC1_copper = SMC1_copper;
        this.SMC1_arsenic = SMC1_arsenic;
        this.SMC1_sodium = SMC1_sodium;
        this.SMC2_ph = SMC2_ph;
        this.SMC1_tkn = SMC1_tkn;
        this.SMC1_sulphates = SMC1_sulphates;
        this.SMC1_zink = SMC1_zink;
        this.SMC5_oilAndGrease = SMC5_oilAndGrease;
        this.SMC5_tds = SMC5_tds;
        HashMap<String, SMC> hashmap = new HashMap<String, SMC>();
        hashmap.put("SMC1_cod", SMC1_cod);
        hashmap.put("SMC1_bod", SMC1_bod);
        hashmap.put("SMC2_temperature", SMC2_temperature);
        hashmap.put("SMC1_chlorides", SMC1_chlorides);
        hashmap.put("SMC1_copper", SMC1_copper);
        hashmap.put("SMC1_arsenic", SMC1_arsenic);
        hashmap.put("SMC1_sodium", SMC1_sodium);
        hashmap.put("SMC2_ph", SMC2_ph);
        hashmap.put("SMC1_tkn", SMC1_tkn);
        hashmap.put("SMC1_sulphates", SMC1_sulphates);
        hashmap.put("SMC1_zink", SMC1_zink);
        hashmap.put("SMC5_oilAndGrease", SMC5_oilAndGrease);
        hashmap.put("SMC5_tds", SMC5_tds);
        this.hashMap = hashmap;
    }

    public static WasteWaterData fromJson(String jsonString) throws ScriptException {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript");
        String script = "Java.asJSONCompatible(" + jsonString + ")";
        Map<String, Map<String, Double>> result = (Map<String, Map<String, Double>>) engine.eval(script);
        try {
            return new WasteWaterData(new SMC(result.get("SMC1_cod")), new SMC(result.get("SMC1_bod")), new SMC(result.get("SMC1_chlorides")), new SMC(result.get("SMC1_tkn")), new SMC(result.get("SMC1_sulphates")), new SMC(result.get("SMC1_zink")), new SMC(result.get("SMC1_copper")), new SMC(result.get("SMC1_arsenic")), new SMC(result.get("SMC1_sodium")), new SMC(result.get("SMC2_ph")), new SMC(result.get("SMC2_temperature")), new SMC(result.get("SMC5_oilAndGrease")), new SMC(result.get("SMC5_tds")));
        } catch (NullPointerException e) {
            throw new NullPointerException("WasteWaterData invalid JSON, missing some field!");
        }
    }

    @Override
    public String toString() {
        return "WasteWaterData{" +
                "SMC1_cod=" + SMC1_cod +
                ", SMC1_bod=" + SMC1_bod +
                ", SMC1_chlorides=" + SMC1_chlorides +
                ", SMC1_tkn=" + SMC1_tkn +
                ", SMC1_sulphates=" + SMC1_sulphates +
                ", SMC1_zink=" + SMC1_zink +
                ", SMC1_copper=" + SMC1_copper +
                ", SMC1_arsenic=" + SMC1_arsenic +
                ", SMC1_sodium=" + SMC1_sodium +
                ", SMC2_ph=" + SMC2_ph +
                ", SMC2_temperature=" + SMC2_temperature +
                ", SMC5_oilAndGrease=" + SMC5_oilAndGrease +
                ", SMC5_tds=" + SMC5_tds +
                '}';
    }

    public SMC getSMC1_cod() {
        return SMC1_cod;
    }

    public SMC getSMC1_bod() {
        return SMC1_bod;
    }

    public SMC getSMC1_chlorides() {
        return SMC1_chlorides;
    }

    public SMC getSMC1_tkn() {
        return SMC1_tkn;
    }

    public SMC getSMC1_sulphates() {
        return SMC1_sulphates;
    }

    public SMC getSMC1_zink() {
        return SMC1_zink;
    }

    public SMC getSMC1_copper() {
        return SMC1_copper;
    }

    public SMC getSMC1_arsenic() {
        return SMC1_arsenic;
    }

    public SMC getSMC1_sodium() {
        return SMC1_sodium;
    }

    public SMC getSMC2_ph() {
        return SMC2_ph;
    }

    public SMC getSMC2_temperature() {
        return SMC2_temperature;
    }

    public SMC getSMC5_oilAndGrease() {
        return SMC5_oilAndGrease;
    }

    public SMC getSMC5_tds() {
        return SMC5_tds;
    }

    public HashMap<String, SMC> getHashMap() {
        return hashMap;
    }
}




