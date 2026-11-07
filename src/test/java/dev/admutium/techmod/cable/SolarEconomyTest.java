package dev.admutium.techmod.cable;
import dev.admutium.techmod.energy.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SolarEconomyTest {
 @Test void daylightHasCorrectPeakAndNoNightGeneration(){assertEquals(1,SolarPower.efficiency(6000,1),1e-9);for(long t:new long[]{0,12000,13000,18000,23999})assertEquals(0,SolarPower.efficiency(t,1),1e-9);assertEquals(.4,SolarPower.efficiency(6000,.4),1e-9);assertEquals(.1,SolarPower.efficiency(6000,.1),1e-9);}
 @Test void clearDayEnergyMatchesAgreedEconomy(){long total=0;for(int tick=0;tick<24000;tick++)total+=Math.round(SolarPower.efficiency(tick,1)*SolarPower.MAX_PER_TICK);assertEquals(190.99,total/(double)Energy.UNIT,.1);assertEquals(10*Energy.UNIT,SolarPower.CAPACITY);}
 @Test void displayedIntegersRoundInsteadOfTruncate(){assertEquals("4",Energy.format(Math.round(3.99*Energy.UNIT)));assertEquals("3",Energy.format(Math.round(3.49*Energy.UNIT)));assertEquals("4",Energy.format(Math.round(3.5*Energy.UNIT)));assertEquals("3.99",Energy.format(Math.round(3.99*Energy.UNIT),true));}
}
