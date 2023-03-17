package it.polimi.tgolfetto.model;

import java.util.Map;

public class SMC {
    private double naturalFiberManufacturingValue;
    private double syntheticFiberManufacturingValue;
    private double spinningValue;
    private double weavingValue;
    private double dyeingValue;
    private double cuttingSewingValue;

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
    }

    public SMC(Map<String, Double> map) {
        try {
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
}
