# Alfresco MCP Server

This service provides a REST API interface to interact with Alfresco repositories using the **Model Context Protocol Server**. It supports operations such as reading node metadata, downloading node content, and searching nodes. The service is built using the `@modelcontextprotocol/sdk` and communicates via `stdio`.

## Features

- **Read Node Metadata**: Retrieve metadata for a specific Alfresco node.
- **Download Node Content**: Download the content of a specific Alfresco node.
- **Search Nodes**: Perform advanced searches for files in Alfresco.
- **Tool Integration**: Supports tools like `search` and `readContent` for integration with the Model Context Protocol.

## Prerequisites

Before running the service, ensure you have the following:

1. **Node.js**: Version 16 or higher.
2. **Alfresco Repository**: A running Alfresco instance with REST API access.
3. **Environment Variables**: Set up the required environment variables (see [Configuration](#configuration)).

## Setup and Installation

1. Clone the repository or download the service script.
2. Install dependencies:
   ```bash
   npm install dotenv @modelcontextprotocol/sdk
   ```
3. Set up the required environment variables (see [Configuration](#configuration)).
4. Run the service:
   ```bash
   node alfresco-rest-server.js
   ```

## Configuration

The service requires the following environment variables to be set:

| Variable Name       | Description                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| `ALFRESCO_HOST`     | The base URL of the Alfresco repository (e.g., `https://alfresco.example.com`). |
| `ALFRESCO_USERNAME` | The username for Alfresco authentication.                                   |
| `ALFRESCO_PASSWORD` | The password for Alfresco authentication.                                   |

### Example `.env` File

```env
ALFRESCO_HOST=http://localhost:8080
ALFRESCO_USERNAME=admin
ALFRESCO_PASSWORD=admin
```

## API Endpoints

The service implements the following functionality through the Model Context Protocol:

### 1. **Read Resource**
- **Description**: Retrieves metadata or content for a specific Alfresco node.
- **Request Schema**: `ReadResourceRequestSchema`
- **Example URI**: `alfresco://{nodeId}`

### 2. **List Tools**
- **Description**: Lists available tools for interacting with Alfresco.
- **Request Schema**: `ListToolsRequestSchema`
- **Supported Tools**:
  - `search`: Performs a full-text search in Alfresco.
  - `readContent`: Reads the content of a file by its Alfresco URI.

### 3. **Call Tool**
- **Description**: Executes a specific tool (e.g., `search` or `readContent`).
- **Request Schema**: `CallToolRequestSchema`

## Troubleshooting

1. **Service Fails to Start**:
   - Ensure all dependencies are installed (`npm install`).
   - Verify that the required environment variables are set.

2. **API Requests Fail**:
   - Check the Alfresco repository logs for errors.
   - Verify that the provided credentials (`ALFRESCO_USERNAME` and `ALFRESCO_PASSWORD`) are correct.

3. **Invalid Node ID**:
   - Ensure the node ID in the URI (e.g., `alfresco://{nodeId}`) is valid and exists in the repository.

4. **Search Returns No Results**:
   - Verify the search query and ensure the repository contains matching files.