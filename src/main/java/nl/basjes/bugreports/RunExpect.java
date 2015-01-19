package nl.basjes.bugreports;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.*;

public class RunExpect {

    public static void main(String[] args) throws IOException {
        RunExpect re = new RunExpect();

        long runs = 0;
        long ok = 0;

        for (int i = 0; i < 1000; i++) {
            runs++;
            if (re.runExpect()) {
                ok++;
            }
        }

        System.out.println("Result: " + ok + "/" + runs);
    }

    public boolean runExpect()
            throws IOException {
        // use option to provide "script" via stdin
        final Commandline cl = new Commandline();
        cl.setExecutable("expect");
        cl.createArg().setValue("-c");
        cl.createArg().setValue("send_user \"ONE \"");

//        cl.createArg().setValue( "-c" );
//        cl.createArg().setValue( "sleep 1" );

        cl.createArg().setValue("-");

        final LogStreamConsumer stdout = new LogStreamConsumer();
        final LogStreamConsumer stderr = new LogStreamConsumer();

        try {
            final InputStream is = new ByteArrayInputStream(writeExpectScriptFile());

            int result = CommandLineUtils.executeCommandLine(cl, is, stdout, stderr);
            if (result != 0) {
                throw new IllegalStateException("Expect execution returned: \'" + result + "\' executing \'"
                        + cl.toString() + "\'");
            }
        } catch (CommandLineException e) {
            final IllegalStateException ise = new IllegalStateException("Unable to run expect");
            ise.initCause(e);
            throw ise;
        }

        return ("ONE TWO".equals(stdout.getLog()));
    }

    /**
     * Writes the expect "script".
     *
     * @return The expect script as a {@code byte[]}.
     * @throws IOException
     */
    private byte[] writeExpectScriptFile()
            throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);

        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos));
        try {
            writer.println("send_user \"TWO\n\"");
            writer.println();
            writer.flush();
        } finally {
            writer.close();
        }

        return baos.toByteArray();
    }

    class LogStreamConsumer
            implements StreamConsumer {
        private final StringBuilder log = new StringBuilder(1024);

        public void consumeLine(String line) {
            log.append(line);
        }

        public String getLog() {
            return log.toString();
        }
    }
}
