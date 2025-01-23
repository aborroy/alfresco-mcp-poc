package com.hyland;

import java.time.Duration;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.client.transport.ServerParameters;
import org.springframework.ai.mcp.client.transport.StdioClientTransport;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main application class for interacting with an AI model and Alfresco services.
 * This class initializes the MCP client, sets up predefined questions, and retrieves responses from the AI model.
 */
@SpringBootApplication
@Configuration
public class Application {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Bean definition for running predefined questions using the ChatClient.
     *
     * @param chatClientBuilder Builder for creating the ChatClient.
     * @param functionCallbacks List of McpFunctionCallbacks for the ChatClient.
     * @param context           ConfigurableApplicationContext to close the application after execution.
     * @return CommandLineRunner instance.
     */
    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
                                                 List<McpFunctionCallback> functionCallbacks,
                                                 ConfigurableApplicationContext context) {

        return args -> {
            try (context) {
                var chatClient = chatClientBuilder
                        .defaultFunctions(functionCallbacks.toArray(new McpFunctionCallback[0]))
                        .build();

                System.out.println("Running predefined questions with AI model responses:\n");

                // Question 1: Retrieve a list of Invoice documents with Alfresco URIs
                String question1 = "Get a list with all the Invoice documents together with the Alfresco URI.";
                System.out.println("QUESTION: " + question1);
                System.out.println("ASSISTANT: " + chatClient.prompt(question1).call().content());

                // Question 2: Provide a summary of a specific invoice
                String question2 = "Provide the total cost and the due date of the invoice 'alfresco://723a0cff-3fce-495d-baa3-a3cd245ea5dc'.";
                System.out.println("\nQUESTION: " + question2);
                System.out.println("ASSISTANT: " + chatClient.prompt(question2).call().content());

            } catch (Exception e) {
                System.err.println("An error occurred while processing the questions: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    /**
     * Bean definition for creating a list of McpFunctionCallbacks.
     *
     * @param mcpClient McpSyncClient instance to list tools.
     * @return List of McpFunctionCallbacks.
     */
    @Bean
    public List<McpFunctionCallback> functionCallbacks(McpSyncClient mcpClient) {
        return mcpClient.listTools(null)
                .tools()
                .stream()
                .map(tool -> new McpFunctionCallback(mcpClient, tool))
                .toList();
    }

    /**
     * Bean definition for creating and initializing the McpSyncClient.
     *
     * @param nodePath         Path to the node executable.
     * @param alfrescoHost     Alfresco host URL.
     * @param alfrescoUsername Alfresco username.
     * @param alfrescoPassword Alfresco password.
     * @param restServerPath   Path to the REST server script.
     * @return Initialized McpSyncClient instance.
     */
    @Bean(destroyMethod = "close")
    public McpSyncClient mcpClient(@Value("${node.path}") String nodePath,
                                   @Value("${alfresco.host}") String alfrescoHost,
                                   @Value("${alfresco.username}") String alfrescoUsername,
                                   @Value("${alfresco.password}") String alfrescoPassword,
                                   @Value("${alfresco.rest-server.path}") String restServerPath) {

        try {
            // Construct the command to set environment variables and start the REST server
            String command = String.join(" && ",
                    "export ALFRESCO_HOST=\"" + alfrescoHost + "\"",
                    "export ALFRESCO_USERNAME=\"" + alfrescoUsername + "\"",
                    "export ALFRESCO_PASSWORD=\"" + alfrescoPassword + "\"",
                    nodePath + " " + restServerPath
            );

            // Configure server parameters for the StdioClientTransport
            var stdioParams = ServerParameters.builder("/bin/bash")
                    .args("-c", command)
                    .build();

            // Create and initialize the McpSyncClient
            var mcpClient = McpClient.using(new StdioClientTransport(stdioParams))
                    .requestTimeout(Duration.ofSeconds(10))
                    .sync();

            var init = mcpClient.initialize();
            System.out.println("MCP Initialized: " + init);

            return mcpClient;
        } catch (Exception e) {
            System.err.println("Failed to initialize MCP client: " + e.getMessage());
            throw new RuntimeException("MCP client initialization failed", e);
        }
    }
}