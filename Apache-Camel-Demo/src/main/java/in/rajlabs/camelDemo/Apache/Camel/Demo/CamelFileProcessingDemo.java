package in.rajlabs.camelDemo.Apache.Camel.Demo;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelFileProcessingDemo {
    public static void main(String[] args) throws Exception {
        // Create a CamelContext to manage the route
        CamelContext context = new DefaultCamelContext();

        // Add a route to read files from 'input-folder', transform them, and log the result
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Read files from 'input-folder'
                from("file:input-folder?noop=true") // noop=true ensures that the files are not moved or deleted after processing
                        .log("Reading file: ${header.CamelFileName}")  // Log the name of the file being processed
                        .convertBodyTo(String.class)  // Convert the file content to String
                        .log("Processed file content: ${body}")  // Log the processed content
                        .to("rabbitmq://localhost:5671/camelExchange?queue=camel&autoDelete=false")
                        .log("send file content to RabbitMq")  // Log the processed content

                        .to("file:output-folder")  // Save the processed content to 'output-folder'
                        .log("File written to output-folder: ${header.CamelFileName}");  // Log that the processed file has been written

                from("rabbitmq://localhost:5671/camelExchange?queue=rcQ&autoDelete=false")  // Consume from RabbitMQ (exchange: camelExchange, queue: camel)
                        .log("Received message from RabbitMQ: ${body}")  // Log the received message
                        .setHeader("CamelFileName", simple("message-${date:now:yyyyMMdd-HHmmssSSS}.txt"))  // Generate a unique filename using timestamp
                        .to("file:output-folder")  // Save the message content as a .txt file in 'output-folder'
                        .log("Saved message to output-folder with filename: ${header.CamelFileName}");

            }
        });

        while (true)
            context.start();


    }
}
