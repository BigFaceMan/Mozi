'''
Author: ssp
Date: 2025-03-18 09:47:02
LastEditTime: 2025-03-18 10:17:01
'''
import asyncio
import websockets
import json

async def handler(websocket, path):
    """处理 WebSocket 连接"""
    try:
        async for message in websocket:
            print(f"收到客户端消息: {message}")
            jsonMap = json.loads(message)
            print("dict : ", jsonMap)
            response = f"服务器收到: {message}"
            await websocket.send(response)
    except websockets.exceptions.ConnectionClosed:
        print("客户端断开连接")

async def main():
    server = await websockets.serve(handler, "localhost", 8765)
    print("WebSocket 服务器已启动，监听端口 8765")
    await server.wait_closed()

if __name__ == "__main__":
    asyncio.run(main())
