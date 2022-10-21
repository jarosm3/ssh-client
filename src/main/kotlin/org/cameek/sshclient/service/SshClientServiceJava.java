package org.cameek.sshclient.service;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("unused")
public class SshClientServiceJava {

    private static final Logger log = LoggerFactory.getLogger(SshClientServiceJava.class);

    private static final String[] IGNORE_STRS = new String[] { "tput[:] unknown terminal [\"]dummy[\"](\r?)(\n?)" };

//    private
//
//    public SshClientService(@Autowired ) {
//    }

    public String listFolderStructure(String username, String password,
                                           String host, int port, long defaultTimeoutSeconds, String command) throws IOException {

        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        try (ClientSession session = client.connect(username, host, port)
                .verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {
            session.addPasswordIdentity(password);
            session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createChannel(Channel.CHANNEL_SHELL)) {
                channel.setOut(responseStream);
                try {
                    channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                    try (OutputStream pipedIn = channel.getInvertedIn()) {
                        pipedIn.write(command.getBytes());
                        pipedIn.flush();
                    }

                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED, ClientChannelEvent.EXIT_STATUS,
                                    ClientChannelEvent.EOF, ClientChannelEvent.EXIT_SIGNAL, ClientChannelEvent.STDERR_DATA,
                                    ClientChannelEvent.STDOUT_DATA),
                            TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds));
                    final String responseString = responseStream.toString();

                    log.debug("Received without filtering: " + responseString);

                    String filteredResponseString = responseString;
                    for (final String str : IGNORE_STRS) {
                        filteredResponseString = filteredResponseString.replaceAll(str, "");
                    }

                    log.debug("Received filtered: " + filteredResponseString);

                    return filteredResponseString;

//                    if (Arrays.stream(IGNORE_STRS).anyMatch(
//                            p -> p.trim().toLowerCase().contains(responseString.trim().toLowerCase())
//                    )) {
//                        log.info("Received: " + responseString);
//                    } else {
//                        log.debug("Received, but ignored: " + responseString);
//                    }

                } finally {
                    channel.close(false);
                }
            }
        } finally {
            client.stop();
        }
    }

}
