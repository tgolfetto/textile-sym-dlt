package it.polimi.tgolfetto.model;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TextileData implements Serializable {
    /*
    Sustainability Macro Categories (SMCs) contained into the Sustainability Data Matrix developed by UNECE:
    - Hazardous chemicals (SMC 1)
    - Water consumption and pollution (SMC 2)
    - GHG emissions and air pollution (SMC 3)
    - Energy consumption and efficiency (SMC 4)
    - Solid waste (SMC 5)
     */

    private final SMC SMC1_enzymatic; //Phthalates
    private final SMC SMC1_antibodyBased; //Organotin compounds
    private final SMC SMC2_arsenic; //Arsenic
    private final SMC SMC2_aox; //Halogenated Organic Compounds
    private final SMC SMC2_ph; //PH of wastewater
    private final SMC SMC3_voc; //Volatile organic compounds
    private final SMC SMC3_no2; //Nitrogen dioxide
    private final SMC SMC3_formaldehyde; //Formaldehyde
    private final SMC SMC4_renewableEnergyPerc; //% non-renewable energy on total energy used
    private final SMC SMC5_solidFlow; //% of regenerated fibers in input/output
    private final HashMap<String, SMC> hashMap;

    public TextileData(SMC SMC1_enzymatic, SMC SMC1_antibodyBased, SMC SMC2_arsenic, SMC SMC2_aox, SMC SMC2_ph, SMC SMC3_voc, SMC SMC3_no2, SMC SMC3_formaldehyde, SMC SMC4_renewableEnergyPerc, SMC SMC5_solidFlow) {
        this.SMC1_enzymatic = SMC1_enzymatic;
        this.SMC1_antibodyBased = SMC1_antibodyBased;
        this.SMC2_arsenic = SMC2_arsenic;
        this.SMC2_aox = SMC2_aox;
        this.SMC2_ph = SMC2_ph;
        this.SMC3_voc = SMC3_voc;
        this.SMC3_no2 = SMC3_no2;
        this.SMC3_formaldehyde = SMC3_formaldehyde;
        this.SMC4_renewableEnergyPerc = SMC4_renewableEnergyPerc;
        this.SMC5_solidFlow = SMC5_solidFlow;
        HashMap<String, SMC> hashMap = new HashMap<String, SMC>();
        hashMap.put("SMC1_enzymatic", SMC1_enzymatic);
        hashMap.put("SMC1_antibodyBased", SMC1_antibodyBased);
        hashMap.put("SMC2_arsenic", SMC2_arsenic);
        hashMap.put("SMC2_aox", SMC2_aox);
        hashMap.put("SMC2_ph", SMC2_ph);
        hashMap.put("SMC3_voc", SMC3_voc);
        hashMap.put("SMC3_no2", SMC3_no2);
        hashMap.put("SMC3_formaldehyde", SMC3_formaldehyde);
        hashMap.put("SMC4_renewableEnergyPerc", SMC4_renewableEnergyPerc);
        hashMap.put("SMC5_solidFlow", SMC5_solidFlow);
        this.hashMap = hashMap;
    }

    public static TextileData fromJson(String jsonString) throws ScriptException {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript");
        String script = "Java.asJSONCompatible(" + jsonString + ")";
        Map<String, Map<String, Double>> result = (Map<String, Map<String, Double>>) engine.eval(script);
        try {
            return new TextileData(new SMC(result.get("SMC1_enzymatic")), new SMC(result.get("SMC1_antibodyBased")), new SMC(result.get("SMC2_arsenic")), new SMC(result.get("SMC2_aox")),
                    new SMC(result.get("SMC2_ph")), new SMC(result.get("SMC3_voc")), new SMC(result.get("SMC3_no2")), new SMC(result.get("SMC3_formaldehyde")),
                    new SMC(result.get("SMC4_renewableEnergyPerc")), new SMC(result.get("SMC5_solidFlow")));
        } catch (NullPointerException e) {
            throw new NullPointerException("TextileData invalid JSON, missing some field!");
        }
    }

    @Override
    public String toString() {
        return "TextileData{" +
                "\nSMC1_enzymatic=" + SMC1_enzymatic +
                ",\n SMC1_antibodyBased=" + SMC1_antibodyBased +
                ",\n SMC2_arsenic=" + SMC2_arsenic +
                ",\n SMC2_aox=" + SMC2_aox +
                ",\n SMC2_ph=" + SMC2_ph +
                ",\n SMC3_voc=" + SMC3_voc +
                ",\n SMC3_no2=" + SMC3_no2 +
                ",\n SMC3_formaldehyde=" + SMC3_formaldehyde +
                ",\n SMC4_renewableEnergyPerc=" + SMC4_renewableEnergyPerc +
                ",\n SMC5_solidFlow=" + SMC5_solidFlow +
                "\n}";
    }

    public SMC getSMC1_enzymatic() {
        return SMC1_enzymatic;
    }

    public SMC getSMC1_antibodyBased() {
        return SMC1_antibodyBased;
    }

    public SMC getSMC2_arsenic() {
        return SMC2_arsenic;
    }

    public SMC getSMC2_aox() {
        return SMC2_aox;
    }

    public SMC getSMC2_ph() {
        return SMC2_ph;
    }

    public SMC getSMC3_voc() {
        return SMC3_voc;
    }

    public SMC getSMC3_no2() {
        return SMC3_no2;
    }

    public SMC getSMC3_formaldehyde() {
        return SMC3_formaldehyde;
    }

    public SMC getSMC4_renewableEnergyPerc() {
        return SMC4_renewableEnergyPerc;
    }

    public SMC getSMC5_solidFlow() {
        return SMC5_solidFlow;
    }

    public HashMap<String, SMC> getHashMap() {
        return hashMap;
    }
}




