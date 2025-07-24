import os
from dotenv import load_dotenv
from qdrant_client import QdrantClient, models
from langchain_openai.embeddings import OpenAIEmbeddings
from langchain_qdrant import Qdrant
from langchain.text_splitter import RecursiveCharacterTextSplitter
from PyPDF2 import PdfReader

# 📥 Load .env
load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
QDRANT_URL = os.getenv("QDRANT_URL")
QDRANT_API_KEY = os.getenv("QDRANT_API_KEY")
COLLECTION_NAME = os.getenv("COLLECTION_NAME", "confluence_docs")

def extract_text_from_pdf(pdf_path):
    reader = PdfReader(pdf_path)
    text = ""
    for page in reader.pages:
        page_text = page.extract_text()
        if page_text:
            text += page_text + "\n"
    return text

def chunk_text(text, chunk_size=200, overlap=50):
    splitter = RecursiveCharacterTextSplitter(chunk_size=chunk_size, chunk_overlap=overlap)
    return splitter.split_text(text)

def create_collection_if_not_exists(client, collection_name, vector_size=1536):
    collections = client.get_collections().collections
    if not any(c.name == collection_name for c in collections):
        client.create_collection(
            collection_name=collection_name,
            vectors_config=models.VectorParams(size=vector_size, distance=models.Distance.COSINE)
        )
        print(f"🆕 Created Qdrant collection: {collection_name}")

def push_to_qdrant(chunks):
    embeddings = OpenAIEmbeddings(openai_api_key=OPENAI_API_KEY)
    client = QdrantClient(url=QDRANT_URL, api_key=QDRANT_API_KEY)
    # Create collection if it doesn't exist
    create_collection_if_not_exists(client, COLLECTION_NAME)
    vector_store = Qdrant(client=client, collection_name=COLLECTION_NAME, embeddings=embeddings)
    # 🆕 Add text payload metadata to each chunk
    metadatas = [{"text": chunk} for chunk in chunks]
    vector_store.add_texts(chunks, metadatas=metadatas)
    print(f"✅ Uploaded {len(chunks)} chunks to Qdrant collection: {COLLECTION_NAME}")

def ingest_pdf(pdf_path):
    print("📖 Extracting text from PDF...")
    text = extract_text_from_pdf(pdf_path)
    print("✂️ Splitting text into chunks...")
    chunks = chunk_text(text)
    print("☁️ Uploading to Qdrant Cloud...")
    push_to_qdrant(chunks)
    print("🎉 Done! PDF content is now searchable.")

if __name__ == "__main__":
    pdf_file = input("📂 Enter path to your Confluence PDF: ").strip()
    ingest_pdf(pdf_file)