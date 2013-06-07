package de.tum.ascodt.repository.entities;

public interface SocketComponent {
	public void open();
	public void close();
	public String getHost();
	public int getPort();
}
