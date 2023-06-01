package it.polimi.tgolfetto.contracts;

import it.polimi.tgolfetto.model.SMC;
import it.polimi.tgolfetto.model.WasteWaterData;
import net.corda.core.identity.Party;
import org.junit.Test;

import javax.script.ScriptException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class StateTests {

    @Test
    public void SMCTest() {
        SMC smc = new SMC(1.0,2.0,3.0,4.0,5.0,6.0);
        HashMap<String,Double> map = new HashMap<>();
        map.put("naturalFiberManufacturingValue", 1.0);
        map.put("syntheticFiberManufacturingValue", 2.0);
        map.put("spinningValue", 3.0);
        map.put("weavingValue", 4.0);
        map.put("dyeingValue", 5.0);
        map.put("cuttingSewingValue", 6.0);
        SMC smc1 = new SMC(map);
        assert(smc.toString().length() > 20);
        assert(smc.getHashMap().get("weavingValue") == 4.0);
        assert(smc.getNaturalFiberManufacturingValue() == 1.0);
        assert(smc.getSyntheticFiberManufacturingValue() == 2.0);
        assert(smc.getSpinningValue() == 3.0);
        assert(smc.getWeavingValue() == 4.0);
        assert(smc.getDyeingValue() == 5.0);
        assert(smc.getCuttingSewingValue() == 6.0);
    }

    private String wasteWaterJson ="{\n" +
            "  \"SMC1_cod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 17.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 10.0,\n" +
            "    \"spinningValue\": 12.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 12.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_bod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 8000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 9000.0,\n" +
            "    \"spinningValue\": 4000.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 4300.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_chlorides\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1150.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1400.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_copper\": {\n" +
            "    \"naturalFiberManufacturingValue\": 3.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 2.0,\n" +
            "    \"cuttingSewingValue\": 2.0\n" +
            "  },\n" +
            "  \"SMC1_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.1,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.12,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sodium\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2040.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1700.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_tkn\": {\n" +
            "    \"naturalFiberManufacturingValue\": 20.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 30.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 43.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sulphates\": {\n" +
            "    \"naturalFiberManufacturingValue\": 600.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 500.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_zink\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.7,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1.9,\n" +
            "    \"cuttingSewingValue\": 1.0\n" +
            "  },\n" +
            "  \"SMC2_temperature\": {\n" +
            "    \"naturalFiberManufacturingValue\": 33.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 33.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 32.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 8.5,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 7.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_oilAndGrease\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 29.0,\n" +
            "    \"weavingValue\": 38.0,\n" +
            "    \"dyeingValue\": 35.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_tds\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1900.0,\n" +
            "    \"spinningValue\": 1800.0,\n" +
            "    \"weavingValue\": 2030.0,\n" +
            "    \"dyeingValue\": 1750.0,\n" +
            "    \"cuttingSewingValue\": 1400.0\n" +
            "  }\n" +
            "}";

    @Test
    public void WasteWaterDataTest() throws ScriptException {
        SMC smc = new SMC(1.0,2.0,3.0,4.0,5.0,6.0);
        WasteWaterData wasteWaterData = new WasteWaterData(smc, smc, smc, smc, smc, smc, smc, smc, smc, smc, smc, smc, smc);
        WasteWaterData wasteWaterData1 = WasteWaterData.fromJson(wasteWaterJson);
        try{
            WasteWaterData wasteWaterData2 = WasteWaterData.fromJson("{fakeJSON: 1.0}");
        }catch (Exception e){
            assert(e.getMessage().equals("WasteWaterData invalid JSON, missing some field!"));
        }
        assert(wasteWaterData.toString().length() > 100);
        assert(wasteWaterData1.getHashMap().get("SMC5_oilAndGrease").getCuttingSewingValue() >= 0.0);
        assert(wasteWaterData.getSMC1_cod().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_bod().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_chlorides().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_tkn().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_sulphates().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_zink().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_copper().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_arsenic().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC1_sodium().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC2_ph().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC2_temperature().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC5_oilAndGrease().getCuttingSewingValue() == 6.0);
        assert(wasteWaterData.getSMC5_tds().getCuttingSewingValue() == 6.0);
    }
}