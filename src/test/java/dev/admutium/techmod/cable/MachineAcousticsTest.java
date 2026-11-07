package dev.admutium.techmod.cable;
import dev.admutium.techmod.machine.MachineAcoustics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MachineAcousticsTest {
 @Test void hardRangeAndMuteApplyRegardlessOfOtherSettings(){assertEquals(0,MachineAcoustics.gain(6,1,false,1));assertEquals(0,MachineAcoustics.gain(40,1,false,1));assertEquals(0,MachineAcoustics.gain(1,0,false,1));}
 @Test void soundFallsOffQuicklyAndWallsMuffleIt(){double close=MachineAcoustics.gain(2,.35,false,1);assertEquals(close/4,MachineAcoustics.gain(4,.35,false,1),1e-9);assertEquals(close/5,MachineAcoustics.gain(2,.35,true,1),1e-9);assertTrue(MachineAcoustics.gain(5.9,.35,false,1)<.001);}
 @Test void multipleMachinesDoNotMultiplyTotalLoudness(){assertEquals(MachineAcoustics.gain(1,.35,false,1)/2,MachineAcoustics.gain(1,.35,false,4),1e-9);}
}
