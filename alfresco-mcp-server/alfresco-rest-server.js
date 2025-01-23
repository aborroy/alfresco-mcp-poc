#!/usr/bin/env node
/**
 * Alfresco REST API Server implementing Model Context Protocol Server
 */

import 'dotenv/config';
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  ReadResourceRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";

// Configuration validation
function validateConfig() {
  const requiredEnvVars = ['ALFRESCO_HOST', 'ALFRESCO_USERNAME', 'ALFRESCO_PASSWORD'];
  
  for (const varName of requiredEnvVars) {
    if (!process.env[varName]) {
      console.error(`Missing required environment variable: ${varName}`);
      process.exit(1);
    }
  }

  try {
    new URL(process.env.ALFRESCO_HOST);
  } catch {
    console.error('Invalid Alfresco host URL');
    process.exit(1);
  }

  return {
    ALFRESCO_HOST: process.env.ALFRESCO_HOST,
    ALFRESCO_USERNAME: process.env.ALFRESCO_USERNAME,
    ALFRESCO_PASSWORD: process.env.ALFRESCO_PASSWORD
  };
}

const {
  ALFRESCO_HOST, 
  ALFRESCO_USERNAME, 
  ALFRESCO_PASSWORD 
} = validateConfig();

// Authentication and API helpers
class AlfrescoAPIClient {
  constructor(host, username, password) {
    this.host = host;
    this.basicAuthHeader = `Basic ${Buffer.from(`${username}:${password}`).toString("base64")}`;
  }

  _buildUrl(path, apiType = 'alfresco') {
    const basePathMap = {
      'alfresco': `/alfresco/api/-default-/public/alfresco/versions/1${path}`,
      'search': '/alfresco/api/-default-/public/search/versions/1/search'
    };
    return `${this.host}${basePathMap[apiType]}`;
  }

  async _fetchWithAuth(url, options = {}) {
    const defaultHeaders = {
      Authorization: this.basicAuthHeader,
      Accept: 'application/json',
      ...options.headers
    };

    try {
      const response = await fetch(url, { 
        ...options, 
        headers: defaultHeaders 
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }

      return response;
    } catch (error) {
      console.error('API Request Failed:', error);
      throw error;
    }
  }

  async getNodeMetadata(nodeId) {
    const url = this._buildUrl(`/nodes/${nodeId}`);
    const response = await this._fetchWithAuth(url);
    return response.json();
  }

  async downloadNodeContent(nodeId) {
    const url = this._buildUrl(`/nodes/${nodeId}/content`);
    return this._fetchWithAuth(url);
  }

  async searchNodes(query, options = {}) {
    const url = this._buildUrl('', 'search');
    const body = {
      query: {
        query: `${query} AND TYPE:'cm:content'`,
        language: 'afts',
      },
      paging: {
        maxItems: options.maxItems || 10,
        skipCount: options.skipCount || 0,
      },
      include: ['properties'],
    };

    const response = await this._fetchWithAuth(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });

    return response.json();
  }
}

const apiClient = new AlfrescoAPIClient(ALFRESCO_HOST, ALFRESCO_USERNAME, ALFRESCO_PASSWORD);

// Server configuration
const server = new Server(
  {
    name: "alfresco-rest-server",
    version: "0.2.0",
  },
  {
    capabilities: {
      resources: {},
      tools: {},
    },
  }
);

// Enhanced request handlers with improved error management
server.setRequestHandler(ReadResourceRequestSchema, async (request) => {
  try {
    const nodeId = request.params.uri.replace("alfresco://", "");
    const metadata = await apiClient.getNodeMetadata(nodeId);
    const entry = metadata.entry;

    if (entry.isFolder) {
      return {
        contents: [{
          uri: request.params.uri,
          mimeType: "text/plain",
          blob: Buffer.from(`Folder: ${nodeId}`).toString('base64')
        }]
      };
    }

    const downloadResp = await apiClient.downloadNodeContent(nodeId);
    const mimeType = entry?.content?.mimeType || "application/octet-stream";
    const arrayBuffer = await downloadResp.arrayBuffer();

    return {
      contents: [{
        uri: request.params.uri,
        mimeType,
        blob: Buffer.from(arrayBuffer).toString('base64')
      }]
    };
  } catch (error) {
    console.error('Read Resource Error:', error);
    throw error;
  }
});

// Tool definitions remain similar to original implementation
server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: [
    {
      name: "search",
      description: "Advanced Alfresco file search",
      inputSchema: {
        type: "object",
        properties: {
          query: { type: "string", description: "Full-text search query" },
          maxItems: { type: "number", description: "Maximum search results" }
        },
        required: ["query"]
      }
    },
    {
      name: "readContent",
      description: "Read file content by Alfresco URI",
      inputSchema: {
        type: "object",
        properties: {
          fileUri: { type: "string", description: "Alfresco file URI" }
        },
        required: ["fileUri"]
      }
    }
  ]
}));

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  switch(name) {
    case "readContent": {
      const nodeId = args.fileUri.replace("alfresco://", "");
      const response = await apiClient.downloadNodeContent(nodeId);
      const content = await response.text();
      
      return {
        content: [{ type: "text", text: content }],
        isError: false
      };
    }
    
    case "search": {
      const result = await apiClient.searchNodes(args.query, {
        maxItems: args.maxItems
      });

      const entries = result.list?.entries || [];
      const totalItems = result.list?.pagination?.totalItems ?? 0;

      const searchResults = entries.map(item => ({
        uri: `alfresco://${item.entry.id}`,
        mimeType: item.entry.content?.mimeType || "application/octet-stream",
        name: item.entry.name
      }));

      return {
        content: [{
          type: "text", 
          text: `Found ${totalItems} items:\n${JSON.stringify(searchResults, null, 2)}`
        }],
        isError: false
      };
    }

    default:
      throw new Error(`Unsupported tool: ${name}`);
  }
});

// Server initialization
async function runServer() {
  console.error("Initializing Alfresco REST Server...");
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Server ready.");
}

runServer().catch(err => {
  console.error("Server initialization failed:", err);
  process.exit(1);
});