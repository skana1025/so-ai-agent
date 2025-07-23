
About:

# SO AI Agent 🤖

An AI assistant for querying resources and knowledge.

## Modules
- `data/` – Python ingestion module (Confluence PDF → Qdrant vectors)
- `backend/` – Java Spring Boot API (coming soon)
- `frontend/` – Angular chat UI (coming soon)

## Quick Start: Data Ingestion
1. Copy `.env.example` to `.env` and fill in your API keys.
2. Install Python dependencies:
   ```bash
   cd data
   pip install -r requirements.txt


DEVELOPER NOTES

OPEN AI

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

Curl:
curl https://api.openai.com/v1/responses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer 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" \
  -d '{
    "model": "gpt-4o-mini",
    "input": "write a haiku about ai",
    "store": true
  }'
—————————————
Node:

npm install openai

import OpenAI from "openai";

const openai = new OpenAI({
  apiKey: "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",
});

const response = openai.responses.create({
  model: "gpt-4o-mini",
  input: "write a haiku about ai",
  store: true,
});

response.then((result) => console.log(result.output_text));

——————

Qdrant Cloud


https://4ae6049f-1f42-42b0-80ed-444111f79d9c.us-west-1-0.aws.cloud.qdrant.io

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3MiOiJtIn0.JAUzb8J3E52TbBP5t4pitecDRKSAOsr_g1-VJW9JaQk

Curl command:
curl \
    -X GET 'https://4ae6049f-1f42-42b0-80ed-444111f79d9c.us-west-1-0.aws.cloud.qdrant.io:6333' \
    --header 'api-key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3MiOiJtIn0.JAUzb8J3E52TbBP5t4pitecDRKSAOsr_g1-VJW9JaQk'
