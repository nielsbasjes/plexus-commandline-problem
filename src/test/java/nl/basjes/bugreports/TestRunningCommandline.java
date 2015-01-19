package nl.basjes.bugreports;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class TestRunningCommandline {

    private static final Logger LOG = LoggerFactory.getLogger(TestRunningCommandline.class);
    private static final long RUNS = 10000;

    @Test
    public void runExpectManyTimes() throws IOException {
        TestRunningCommandline re = new TestRunningCommandline();

        long runs = 0;
        long ok = 0;

        for (long i = 0; i < RUNS; i++) {
            runs++;
            if (re.runExpect()) {
                ok++;
            }
            if (runs % 100 == 0) {
                LOG.info("Result: {}/{} = {}%", ok, runs, 100 * ((double)ok/(double)runs));
            }
        }
        LOG.info("Result: {}/{} = {}%", ok, runs, 100 * ((double)ok/(double)runs));
        Assert.assertEquals("All runs MUST be ok",runs,ok);

    }

    public boolean runExpect()
            throws IOException {
        // use option to provide "script" via stdin
        final Commandline cl = new Commandline();
        cl.setExecutable("expect");
        cl.createArg().setValue("-c");
        cl.createArg().setValue("send_user \"ONE \"");

        cl.createArg().setValue("-");

        final LogStreamConsumer stdout = new LogStreamConsumer();
        final LogStreamConsumer stderr = new LogStreamConsumer();

        try {
            final InputStream is = new ByteArrayInputStream(writeExpectScriptFile());
            int result = CommandLineUtils.executeCommandLine(cl, is, stdout, stderr);
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
     * @throws java.io.IOException
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
