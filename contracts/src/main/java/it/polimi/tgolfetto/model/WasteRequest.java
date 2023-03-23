package it.polimi.tgolfetto.model;

public class WasteRequest extends TextileData{

    private boolean send;
    private int qty;
    private String wasteName;

    public WasteRequest(boolean send, int qty, String wasteName, SMC SMC1_enzymatic, SMC SMC1_antibodyBased, SMC SMC2_arsenic, SMC SMC2_aox, SMC SMC2_ph, SMC SMC3_voc, SMC SMC3_no2, SMC SMC3_formaldehyde, SMC SMC4_renewableEnergyPerc, SMC SMC5_solidFlow) {
        super(SMC1_enzymatic, SMC1_antibodyBased, SMC2_arsenic, SMC2_aox, SMC2_ph, SMC3_voc, SMC3_no2, SMC3_formaldehyde, SMC4_renewableEnergyPerc, SMC5_solidFlow);
        this.send = send;
        this.qty = qty;
        this.wasteName = wasteName;
    }

    public boolean isSend() {
        return send;
    }

    public int getQty() {
        return qty;
    }

    public String getWasteName() {
        return wasteName;
    }

    @Override
    public String toString() {
        return "WasteRequest{" +
                "send=" + send +
                ", qty=" + qty +
                ", wasteName='" + wasteName + '\'' +
                ", textileData='" + super.toString() + '\''+
                '}';
    }
}
