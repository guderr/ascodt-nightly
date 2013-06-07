package de.tum.ascodt.plugin.services;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class SocketHandler implements CompletionHandler<AsynchronousSocketChannel, Object>{
	private AsynchronousSocketChannel _socketChannel;
	@Override
	public void completed(AsynchronousSocketChannel result, Object attachment) {
		setSocketChannel(result);
	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the _socketChannel
	 */
	public AsynchronousSocketChannel getSocketChannel() {
		return _socketChannel;
	}

	/**
	 * @param _socketChannel the _socketChannel to set
	 */
	public void setSocketChannel(AsynchronousSocketChannel socketChannel) {
		this._socketChannel = socketChannel;
	}

}
