'''
Author: ssp
Date: 2025-03-18 09:46:29
LastEditTime: 2025-03-18 10:16:51
'''
import asyncio
import websockets

async def client():
    uri = "ws://localhost:8765"
    async with websockets.connect(uri) as websocket:
        while True:
            message = input("请输入要发送的消息（输入 `exit` 退出）：")
            if message.lower() == "exit":
                break
            await websocket.send(message)
            response = await websocket.recv()
            print(f"服务器返回: {response}")

if __name__ == "__main__":
    asyncio.run(client())
