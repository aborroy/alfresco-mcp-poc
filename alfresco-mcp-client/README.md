# Alfresco MCP Client

The Alfresco [MCP Client](https://modelcontextprotocol.io/quickstart/client) is a Spring Boot application designed to interact with Alfresco content management system using an AI model (via Ollama). It allows users to query Alfresco documents and retrieve summaries or other relevant information using natural language prompts.

## Features

- **AI-Powered Queries**: Use an AI model (e.g., Llama3) to interact with Alfresco documents.
- **Predefined Questions**: Execute predefined questions to retrieve specific information from Alfresco.
- **Custom MCP Client**: Integrates with a custom MCP client to interact with Alfresco REST services.
- **Command-Line Interface**: Runs as a command-line application without a web interface.

## Requirements

To run this project, you need the following:

1. **Java Development Kit (JDK) 17 or higher**:
    - Download and install the latest JDK from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) or use an open-source alternative like [OpenJDK](https://openjdk.org/).

2. **Node.js (v18.19.1 or higher)**:
    - Download and install Node.js from [nodejs.org](https://nodejs.org/).

3. **Ollama**:
    - Install and run Ollama locally. Follow the instructions at [Ollama GitHub](https://github.com/ollama/ollama).

4. **Alfresco Instance**:
    - A running instance of Alfresco (e.g., `http://localhost:8080`).

5. **Alfresco REST Server Script**:
    - A Node.js script (`alfresco-rest-server.js`) that acts as a bridge between the MCP client and Alfresco.

## How to Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repo/alfresco-mcp-client.git
   cd alfresco-mcp-client
   ```
2. **Build the Project:**
    Use Maven to build the project:
    ```bash
    ./mvn clean package
    ```

3. **Run the Application:**
    After building, run the application using the generated JAR file:

    ```bash
    java -jar target/alfresco-mcp-client.jar
    ```

## Configuration

The application is configured using the `application.properties file. Below are the key configuration options:

* Spring AI (LLM) Configuration

    spring.ai.ollama.base-url=http://localhost:11434: Base URL for the Ollama API.

    spring.ai.ollama.chat.options.model=llama3.1: Specifies the AI model to use for chat interactions.

* Alfresco MCP Configuration

    node.path=/path/to/node: Path to the Node.js executable.

    alfresco.host=http://localhost:8080: URL of the Alfresco instance.

    alfresco.username=admin: Username for Alfresco authentication.

    alfresco.password=admin: Password for Alfresco authentication.

    alfresco.rest-server.path=/path/to/alfresco-rest-server.js: Path to the Alfresco REST server script.

## How to Run

1. **Ensure Ollama is Running:**

    Start the Ollama server locally:
    ```bash
    ollama serve
    ```

2. Update Configuration:

    Modify the `application.properties` file to match your environment (e.g., Alfresco host, credentials, Node.js path, etc.).

    Run the Application:

    ```bash
    java -jar target/alfresco-mcp-client-0.8.0.jar
    ```

    View Output: The application will execute predefined questions and print the AI model's responses to the console.

## Predefined Questions

The application includes the following predefined questions, but you can experiment your proposals.

    Retrieve Invoice Documents:

        Question: "Get a list with all the Invoice documents together with the Alfresco URI."

        Purpose: Retrieves a list of all invoice documents along with their Alfresco URIs.

    Summarize an Invoice:

        Question: "Provide a summary of the invoice 'alfresco://723a0cff-3fce-495d-baa3-a3cd245ea5dc'."

        Purpose: Provides a summary of a specific invoice document.