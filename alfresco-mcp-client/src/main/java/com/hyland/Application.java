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

@SpringBootApplication
@Configuration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
                                                 List<McpFunctionCallback> functionCallbacks, ConfigurableApplicationContext context) {

        return args -> {
            var chatClient = chatClientBuilder
                    .defaultFunctions(functionCallbacks.toArray(new McpFunctionCallback[0]))
                    .build();

            System.out.println("Running predefined questions with AI model responses:\n");

            // Question 1
            String question1 = "Get a list with all the Invoice documents together with the Alfresco URI.";
            System.out.println("QUESTION: " + question1);
            System.out.println("ASSISTANT: " + chatClient.prompt(question1).call().content());

            // Question 2
            String question2 = "Provide a summary of the invoice 'alfresco://723a0cff-3fce-495d-baa3-a3cd245ea5dc'.";
            System.out.println("\nQUESTION: " + question2);
            System.out.println("ASSISTANT: " +
                    chatClient.prompt(question2).call().content());
            context.close();

        };
    }

    @Bean
    public List<McpFunctionCallback> functionCallbacks(McpSyncClient mcpClient) {

        var callbacks = mcpClient.listTools(null)
                .tools()
                .stream()
                .map(tool -> new McpFunctionCallback(mcpClient, tool))
                .toList();

        return callbacks;
    }

    @Bean(destroyMethod = "close")
    public McpSyncClient mcpClient(@Value("${node.path}") String nodePath,
                                   @Value("${alfresco.host}") String alfrescoHost,
                                   @Value("${alfresco.username}") String alfrescoUsername,
                                   @Value("${alfresco.password}") String alfrescoPassword,
                                   @Value("${alfresco.rest-server.path}") String restServerPath) {

        String command = String.join(" && ",
                "export ALFRESCO_HOST=\"" + alfrescoHost + "\"",
                "export ALFRESCO_USERNAME=\"" + alfrescoUsername + "\"",
                "export ALFRESCO_PASSWORD=\"" + alfrescoPassword + "\"",
                nodePath + " " + restServerPath
        );

        var stdioParams = ServerParameters.builder("/bin/bash")
                .args("-c", command)
                .build();

        var mcpClient = McpClient.using(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(10))
                .sync();

        var init = mcpClient.initialize();

        System.out.println("MCP Initialized: " + init);

        return mcpClient;
    }

}