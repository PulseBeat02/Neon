import io.github.pulsebeat02.neon.utils.ProcessUtils;

public class EnvTest {

    public static void main(final String[] args){
        ProcessUtils.setEnv("hacked_variable", "woohoo");
        System.out.println(System.getenv());
    }

}
