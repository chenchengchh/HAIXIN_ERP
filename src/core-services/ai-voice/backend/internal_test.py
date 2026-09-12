import asyncio
import websockets
import logging
import json

logging.basicConfig(level=logging.INFO)

async def test():
    uri = "ws://localhost:8000/ai-voice/ws/stream-asr"
    print(f"Connecting to {uri}...")
    try:
        async with websockets.connect(uri) as websocket:
            print("Connected!")
            await websocket.send(json.dumps({"type": "ping"}))
            print("Sent ping")
            resp = await websocket.recv()
            print(f"Recv: {resp}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    asyncio.run(test())