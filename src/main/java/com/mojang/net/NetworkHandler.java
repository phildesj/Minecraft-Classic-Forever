package com.mojang.net;

import com.mojang.minecraft.net.NetworkManager;
import com.mojang.minecraft.net.PacketType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * NetworkHandler manages network communication over a non-blocking socket channel.
 * It handles sending and receiving packets with type-safe serialization and deserialization.
 * The handler maintains input and output buffers for efficient network I/O operations.
 */
public final class NetworkHandler {

	// Buffer size for network I/O (1 MB)
	private static final int BUFFER_SIZE = 1048576;

	// String buffer size for network transmission
	private static final int STRING_BUFFER_SIZE = 64;

	// Default padding byte for string buffers
	private static final byte PADDING_BYTE = 32;

	// Connection status flag
	public volatile boolean connected;

	// Non-blocking socket channel for network communication
	public SocketChannel channel;

	// Input buffer for receiving data from the network
	public ByteBuffer in = ByteBuffer.allocate(BUFFER_SIZE);

	// Output buffer for sending data to the network
	public ByteBuffer out = ByteBuffer.allocate(BUFFER_SIZE);

	// Network manager for error handling and event management
	public NetworkManager netManager;

	// Underlying socket for configuration
	private Socket sock;

	// Buffer for string data transmission
	private final byte[] stringBytes = new byte[STRING_BUFFER_SIZE];

	/**
	 * Constructs a NetworkHandler and establishes a connection to the specified host and port.
	 * Configures the socket for low-latency communication with TCP_NODELAY enabled.
	 *
	 * @param host the hostname or IP address to connect to
	 * @param port the port number to connect to
	 */
	public NetworkHandler(String host, int port) {
		try {
			// Open and configure non-blocking socket channel
			channel = SocketChannel.open();
			channel.connect(new InetSocketAddress(host, port));
			channel.configureBlocking(false);

			// Get underlying socket and configure for low-latency communication
			sock = channel.socket();
			sock.setTcpNoDelay(true);
			sock.setTrafficClass(24);
			sock.setKeepAlive(false);
			sock.setReuseAddress(false);
			sock.setSoTimeout(100);

			// Clear buffers and mark as connected
			in.clear();
			out.clear();
			connected = true;
		} catch (IOException e) {
			// Log connection error - connection remains false
			System.err.println("Failed to establish network connection: " + e.getMessage());
		}
	}

	/**
	 * Closes the network connection and flushes any pending output data.
	 * Sets the connected flag to false and cleans up socket resources.
	 */
	public void close() {
		try {
			// Flush any pending output data before closing
			if (out.position() > 0) {
				out.flip();
				channel.write(out);
				out.compact();
			}
		} catch (Exception e) {
			// Ignore exceptions during flush
		}

		connected = false;

		try {
			// Close the socket channel
			channel.close();
		} catch (Exception e) {
			// Ignore exceptions during close
		}

		sock = null;
		channel = null;
	}

	/**
	 * Sends a packet with the specified type and parameters over the network.
	 *
	 * @param packetType the type of packet to send
	 * @param params the parameters to include in the packet
	 */
	public void send(PacketType packetType, Object... params) {
		if (!connected) {
			return;
		}

		// Write packet opcode
		out.put(packetType.opcode);

		// Write each parameter based on its type
		for (int i = 0; i < params.length; ++i) {
			Class<?> paramType = packetType.params[i];
			Object paramValue = params[i];
			writeParameter(paramType, paramValue);
		}
	}

	/**
	 * Helper method to write a single parameter to the output buffer based on its type.
	 * Handles primitive types, String, and byte[] with appropriate serialization.
	 *
	 * @param paramType the Class type of the parameter
	 * @param paramValue the value to write
	 */
	private void writeParameter(Class<?> paramType, Object paramValue) {
		if (!connected) {
			return;
		}

		try {
			// Handle primitive types
			if (paramType == Long.TYPE) {
				out.putLong((Long) paramValue);
			} else if (paramType == Integer.TYPE) {
				out.putInt(((Number) paramValue).intValue());
			} else if (paramType == Short.TYPE) {
				out.putShort(((Number) paramValue).shortValue());
			} else if (paramType == Byte.TYPE) {
				out.put(((Number) paramValue).byteValue());
			} else if (paramType == Double.TYPE) {
				out.putDouble((Double) paramValue);
			} else if (paramType == Float.TYPE) {
				out.putFloat((Float) paramValue);
			} else if (paramType == String.class) {
				// Handle String with UTF-8 encoding and padding
				writeString((String) paramValue);
			} else if (paramType == byte[].class) {
				// Handle byte array with padding to 1024 bytes
				writeByteArray((byte[]) paramValue);
			}
		} catch (Exception e) {
			netManager.error(e);
		}
	}

	/**
	 * Writes a String to the output buffer with UTF-8 encoding and null padding.
	 *
	 * @param value the String to write
	 */
	private void writeString(String value) {
		byte[] encodedBytes = value.getBytes(StandardCharsets.UTF_8);
		Arrays.fill(stringBytes, PADDING_BYTE);

		// Copy string bytes to the fixed-size buffer
		int bytesToCopy = Math.min(STRING_BUFFER_SIZE, encodedBytes.length);
		System.arraycopy(encodedBytes, 0, stringBytes, 0, bytesToCopy);

		out.put(stringBytes);
	}

	/**
	 * Writes a byte array to the output buffer with padding to 1024 bytes.
	 *
	 * @param value the byte array to write
	 */
	private void writeByteArray(byte[] value) {
		byte[] paddedArray = value;
		if (value.length < 1024) {
			paddedArray = Arrays.copyOf(value, 1024);
		}
		out.put(paddedArray);
	}

	/**
	 * Reads an object from the input buffer with automatic type conversion.
	 * Deserializes primitive types, Strings, and byte arrays from network data.
	 *
	 * @param type the Class type to deserialize
	 * @return the deserialized object, or null if not connected or type is unsupported
	 */
	public Object readObject(Class<?> type) {
		if (!connected) {
			return null;
		}

		try {
			// Handle primitive types and their wrappers
			if (type == Long.TYPE) {
				return in.getLong();
			} else if (type == Integer.TYPE) {
				return in.getInt();
			} else if (type == Short.TYPE) {
				return in.getShort();
			} else if (type == Byte.TYPE) {
				return in.get();
			} else if (type == Double.TYPE) {
				return in.getDouble();
			} else if (type == Float.TYPE) {
				return in.getFloat();
			} else if (type == String.class) {
				// Read fixed-size string buffer and decode as UTF-8
				in.get(stringBytes);
				return new String(stringBytes, StandardCharsets.UTF_8).trim();
			} else if (type == byte[].class) {
				// Read fixed-size byte array
				byte[] byteArray = new byte[1024];
				in.get(byteArray);
				return byteArray;
			} else {
				return null;
			}
		} catch (Exception e) {
			netManager.error(e);
			return null;
		}
	}
}
