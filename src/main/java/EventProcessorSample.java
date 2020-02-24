import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.EventProcessorOptions;
import com.microsoft.azure.eventprocessorhost.ExceptionReceivedEventArgs;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class EventProcessorSample
{
    public static void main(String args[]) throws InterruptedException, ExecutionException
    {
        String consumerGroupName = "$Default";
        String namespaceName = "purestream";
        String eventHubName = "receivelog";
        String sasKeyName = "RootManageSharedAccessKey";
        String sasKey = "C8MZwiuIqmBYjsOo8jTnvBmNgUrzGmuNLI5dMXhhvD4=";
        String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=purestreamdiag;AccountKey=k1uamjrhshg0dehFjHGJfCXM16YlGfei7iW4Ivvq9WpCEP7Qjb0uMk+OzZ/8llmgKJljhBG6nOUril5I4Cu8+A==;EndpointSuffix=core.windows.net";
        String storageContainerName = "logs";
        String hostNamePrefix = "purestream";
        ConnectionStringBuilder eventHubConnectionString = new ConnectionStringBuilder()
                .setNamespaceName(namespaceName)
                .setEventHubName(eventHubName)
                .setSasKeyName(sasKeyName)
                .setSasKey(sasKey);

        EventProcessorHost host = new EventProcessorHost(
                EventProcessorHost.createHostName(hostNamePrefix),
                eventHubName,
                consumerGroupName,
                eventHubConnectionString.toString(),
                storageConnectionString,
                storageContainerName);

        System.out.println("Registering host named " + host.getHostName());
        EventProcessorOptions options = new EventProcessorOptions();
        options.setExceptionNotification(new ErrorNotificationHandler());
        //options.setInvokeProcessorAfterReceiveTimeout(true);
        host.registerEventProcessor(EventProcessor.class, options)
                .whenComplete((unused, e) ->
                {
                    if (e != null)
                    {
                        System.out.println("Failure while registering: " + e.toString());
                        if (e.getCause() != null)
                        {
                            System.out.println("Inner exception: " + e.getCause().toString());
                        }
                    }
                })
                .thenAccept((unused) ->
                {
                    System.out.println("Press enter to stop.");
                    try
                    {
                        System.in.read();
                    }
                    catch (Exception e)
                    {
                        System.out.println("Keyboard read failed: " + e.toString());
                    }
                })
                .thenCompose((unused) ->
                {
                    return host.unregisterEventProcessor();
                })
                .exceptionally((e) ->
                {
                    System.out.println("Failure while unregistering: " + e.toString());
                    if (e.getCause() != null)
                    {
                        System.out.println("Inner exception: " + e.getCause().toString());
                    }
                    return null;
                })
                .get(); // Wait for everything to finish before exiting main!

        System.out.println("End of sample");
    }}
