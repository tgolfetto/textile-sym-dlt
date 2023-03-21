package it.polimi.tgolfetto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SMC implements Serializable {
    private final double naturalFiberManufacturingValue;
    private final double syntheticFiberManufacturingValue;
    private final double spinningValue;
    private final double weavingValue;
    private final double dyeingValue;
    private final double cuttingSewingValue;

    private final HashMap<String, Double> hashMap;

    @Override
    public String toString() {
        return "{" +
                "naturalFiberManufacturingValue=" + naturalFiberManufacturingValue +
                ", syntheticFiberManufacturingValue=" + syntheticFiberManufacturingValue +
                ", spinningValue=" + spinningValue +
                ", weavingValue=" + weavingValue +
                ", dyeingValue=" + dyeingValue +
                ", cuttingSewingValue=" + cuttingSewingValue +
                '}';
    }

    /**
     * Generate the info data for a Sustainability Macro Category indicator, values could be null
     *
     * @param naturalFiberManufacturingValue
     * @param syntheticFiberManufacturingValue
     * @param spinningValue
     * @param weavingValue
     * @param dyeingValue
     * @param cuttingSewingValue
     */
    public SMC(double naturalFiberManufacturingValue, double syntheticFiberManufacturingValue, double spinningValue, double weavingValue, double dyeingValue, double cuttingSewingValue) {
        this.naturalFiberManufacturingValue = naturalFiberManufacturingValue;
        this.syntheticFiberManufacturingValue = syntheticFiberManufacturingValue;
        this.spinningValue = spinningValue;
        this.weavingValue = weavingValue;
        this.dyeingValue = dyeingValue;
        this.cuttingSewingValue = cuttingSewingValue;
        HashMap<String, Double> hashMap = new HashMap<String, Double>();
        hashMap.put("naturalFiberManufacturingValue", naturalFiberManufacturingValue);
        hashMap.put("syntheticFiberManufacturingValue", syntheticFiberManufacturingValue);
        hashMap.put("spinningValue", spinningValue);
        hashMap.put("weavingValue", weavingValue);
        hashMap.put("dyeingValue", dyeingValue);
        hashMap.put("cuttingSewingValue", cuttingSewingValue);
        this.hashMap = hashMap;
    }

    public SMC(Map<String, Double> map) {
        try {
            this.hashMap = new HashMap<String, Double>(map);
            this.naturalFiberManufacturingValue = map.get("naturalFiberManufacturingValue");
            this.syntheticFiberManufacturingValue = map.get("syntheticFiberManufacturingValue");
            this.spinningValue = map.get("spinningValue");
            this.weavingValue = map.get("weavingValue");
            this.dyeingValue = map.get("dyeingValue");
            this.cuttingSewingValue = map.get("cuttingSewingValue");
        } catch (NullPointerException e) {
            throw new NullPointerException("SMC invalid JSON, missing some field!");
        }
    }

    public double getNaturalFiberManufacturingValue() {
        return naturalFiberManufacturingValue;
    }

    public double getSyntheticFiberManufacturingValue() {
        return syntheticFiberManufacturingValue;
    }

    public double getSpinningValue() {
        return spinningValue;
    }

    public double getWeavingValue() {
        return weavingValue;
    }

    public double getDyeingValue() {
        return dyeingValue;
    }

    public double getCuttingSewingValue() {
        return cuttingSewingValue;
    }

    public HashMap<String, Double> getHashMap() {
        return hashMap;
    }
}
