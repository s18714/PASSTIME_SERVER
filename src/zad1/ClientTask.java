/**
 * @author Kryzhanivskyi Denys S18714
 */

package zad1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ClientTask extends FutureTask<String> {

    private ClientTask(Callable<String> callable) {
        super(callable);
    }

    public static ClientTask create(Client client, List<String> requests, boolean showSendResult) {
        return new ClientTask(() -> {
            client.connect();
            client.send("login " + client.getClientId());
            for (String req : requests) {
                String res = client.send(req);
                if (showSendResult) System.out.println(res);
            }
            return client.send("bye and log transfer");
        });
    }
}