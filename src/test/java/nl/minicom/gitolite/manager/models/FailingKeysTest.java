package nl.minicom.gitolite.manager.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 * Tests if known invalid keys are rejected
 * @author Jan-Willem Gmelig Meyling
 */

@RunWith(Parameterized.class)
public class FailingKeysTest {

    public final static String INVALID_KEY_1 = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCftV0wGyhXU2aorR2WmJZCCEbzACRxPpGiI5csVz25uEfQ+QpBsTj43rEDkAQPvO6p+$";
    public final static String INVALID_KEY_2 = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCX3E4ePEeuzuJwDB408dkS7gXgfjSWhom03zn2oAz$";
    public final static String INVALID_KEY_3 = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC5nnzP2T+jLLNg/S6ejEx2+Js0PpgdedZzcNpdwMbVMNIOBrtIkyyxPP+ckQd5Ush8D$";
    public final static String INVALID_KEY_4 = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJdlQfcU90mzmVIG4R8QeiqrEI0Kx3KVfM2ux7j/KsN83Pw7BrFvKrlkhfwRjTiQYiwRtVzIVrOlBopel$";
    public final static String INVALID_KEY_5 = "ssh-rsa AAABAAQus1/EbQoJ+3X2gqLdEl+P2o1+Vz3ktV5wtRTAIyWvMIS1UY79RluJM/xylR1b4LroYGXwqCQwX36ep8oPp/taDorawA31Ufe1laC90ji7y213sVVshvgc4Z0kY+8YpAv+1rk8sMD4FG2Cxz3lg8XumLAd4sMNKP4n3B6T4mqrJ/pgNVfRCLYdY24igAbIOOLHLd+TGlPB9yB8fWvlpIcFS1/SENA1hCWuEH/ot/SAypIcEIYcvDia6QcEnSJkQslnr0dFJ62wUFlR2Gqg92XNYe3AsNDuGk0DqwW0AhuMMSGyiBUlUH8xpTi1ttyEAepYNRZsarJGB4GAE22nADUAAACBAPwm9vmk9uO+HxSSNrWPDtiGaqgkr+jQdneDCki/n6Vcr62rbj3sJVhNMPQ4M1NfVX62arSDZby4MjAStAhc/8ORhCizQ+66R025I5yHtpijXcMrMhzEs4n+hfimPSKpxpCdn1wWCMNKW7MWNTnoYd+X9HQB3/ZKdfAf24REz+E7AAAAgQCdHHTe+FEsnZgKPpXRquIGirCUihKAqPPewctEvP/soGEu8Z92fWYy7eEJXOX0btiyP70Ot4DGg2u3tAxfUb7+HMpc/uA8OMEf9QHTiJxxsphdmSHzHDUDaClvrIZi27SgAfIjrLKlKB0rIiJRSkhpLnpRZtOk6KyD662e5wqY5QAAAIBfJTnY39D5Am9VwrSRPp/jGW/5EchqMeTq1AFcGCgt3Q9RhWM3KvCRT+bM3qY8ZJHbZ32epG6V43ueKXADBNb5CXM2PoyJmQlYxVwtwAf2vx54NpmeCkdi8CV9walId8/DMCFI+Pa5NWn5XKIWB+g9Cf41JCMLHoc1AM8g4JHyPA==";
    public final static String INVALID_KEY_6 = "ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAQEAmr/s12PCc3FYDKDhifOnz8qWc0Kb8g42pkor/8UUclIDLjTJqpsrOtSDfI";
    public final static String INVALID_KEY_7 = "ssh-rsa 2048 b2:08:34:36:20:65:a6:64:9c:f5:cc:10:08:68:6d:8f";


    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {INVALID_KEY_1},
            {INVALID_KEY_2},
            {INVALID_KEY_3},
            {INVALID_KEY_4},
            {INVALID_KEY_5},
            {INVALID_KEY_6},
            {INVALID_KEY_7}
        });
    }

    private final String key;
    private final User user;

    /**
     * Construct a new KeyValidationTest
     * @param key Key contents
     */
    public FailingKeysTest(String key) {
        this.key = key;
        this.user = new User("test");
    }

    /**
     * Validate a key
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidKey() {
        user.setKey("keyName", key);
    }

}
