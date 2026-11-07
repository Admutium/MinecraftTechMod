package dev.admutium.techmod.cable;
import dev.admutium.techmod.energy.Energy;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class EnergyEconomyTest {
 @Test void fuelAndRecipeUseExactIntegralAccounting(){assertEquals(100*Energy.UNIT,900*Energy.GENERATION);assertEquals(16*Energy.UNIT,160*Energy.SMELT_TICK);assertTrue(Energy.GENERATION>Energy.SMELT_TICK);}
 @Test void batteryAndEightSlotCabinetMatchAgreedEconomy(){assertEquals(18,Energy.BATTERY/(16*Energy.UNIT));assertEquals(12*Energy.UNIT,Energy.BATTERY%(16*Energy.UNIT));assertEquals(150,8*Energy.BATTERY/(16*Energy.UNIT));}
 @Test void lossIsOnlyDeductedAndNeverProducesEnergy(){for(int sent=1;sent<10000;sent+=13)for(double length:new double[]{0,.3,5,20,500,1000}){long received=Energy.delivered(sent,length);assertTrue(received>=0&&received<=sent);if(length==0)assertEquals(sent,received);}}
 @Test void suspensionChargesRoundedDistanceAndRemembersPaidAmount(){
  Node a=CableLayout.placement(0,0,0,Face.DOWN),b=CableLayout.placement(3,0,4,Face.DOWN);
  CableSpan span=new CableSpan(UUID.randomUUID(),a,b);assertEquals(5,span.cost());assertTrue(span.length()>5);assertEquals(5,CableSpan.decode(span.encode()).cost());
  String legacy=span.id()+";"+a.encode()+";"+b.encode();assertEquals(1,CableSpan.decode(legacy).cost(),"Legacy spans must not refund more than was paid");
 }
 @Test void fractionalSpanLengthRoundsUpWithoutFloatingBoundaryPenalty(){assertEquals(4,Energy.wireCost(3.2));assertEquals(5,Energy.wireCost(5));assertEquals(5,Energy.wireCost(5.00000000001));}
}
