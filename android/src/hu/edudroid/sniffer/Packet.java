package hu.edudroid.sniffer;

import java.nio.ByteBuffer;

/**
 * !!! This is just a utility for processing packets with the least overhead. Doesn't store data.
 * !!! Very single threaded!
 * @author lajthabalazs
 *
 */
public class Packet {
	private static final byte TCP = 6;
	private static final byte UDP = 17;
	private static final int MIN_IP_HEADER_SIZE = 20;
	private final byte ZERO = 0;
	public byte[] sourceIp = new byte[4];
	public int sourcePort; 
	public byte[] destIp = new byte[4];
	public byte protocol;
	public int destPort;
	public int packetLength;
	public int ipHeaderLength;
	public int transportHeaderLength;
	public boolean hasIpOptions = false;
	public short version;
	public short ihl;
	public byte[] data; // A reference to data
	public int dataOffset; // Start of the packet payload in the data array
	public int dataLength; // Length of the packet payload
	
	public boolean parse(ByteBuffer buffer, int packetStart, int lastData) {
		System.arraycopy(buffer.array(), packetStart + 12, sourceIp, 0, 4);
		System.arraycopy(buffer.array(), packetStart + 16, destIp, 0, 4);
		// If there isn't a whole ip header, return
		if (packetStart + MIN_IP_HEADER_SIZE > lastData) {
			return false;
		}
		version = (short)((buffer.array()[0] & 0xF0) >> 4);
		ihl = (short)((buffer.array()[0] & 0x0F));
		packetLength = TCPIPUtils.toIntUnsigned(buffer.array()[packetStart + 2], buffer.array()[packetStart + 3]);
		if (packetStart + packetLength > lastData) {
			return false;
		}
		System.out.println("Length : " + packetLength + " (" + buffer.array()[packetStart + 2] + " " + buffer.array()[packetStart + 3] + ")");
		ipHeaderLength = ihl * 4;
		System.out.println("IP header length : " + ipHeaderLength);
		sourcePort = TCPIPUtils.toIntUnsigned(buffer.array()[packetStart + ipHeaderLength + 1], buffer.array()[packetStart + ipHeaderLength]);
		System.out.println("SourcePort : " + sourcePort + " (" + buffer.array()[packetStart + ipHeaderLength] + " " + buffer.array()[packetStart + ipHeaderLength + 1] + ")");
		destPort =  TCPIPUtils.toIntUnsigned(buffer.array()[packetStart + ipHeaderLength + 2], buffer.array()[packetStart + ipHeaderLength + 3]);
		System.out.println("DestPort : " + destPort + " (" + buffer.array()[packetStart + ipHeaderLength + 2] + " " + buffer.array()[packetStart + ipHeaderLength + 3] + ")");
		protocol = buffer.array()[packetStart + 9];
		if (protocol == UDP) {
			transportHeaderLength = 8;
		} else if (protocol == TCP) {
			transportHeaderLength = TCPIPUtils.toIntUnsigned(ZERO, buffer.array()[packetStart + ipHeaderLength + 12]);
		}
		dataLength = packetLength - (ipHeaderLength + transportHeaderLength);
		dataOffset = packetStart + ipHeaderLength + transportHeaderLength;
		data = buffer.array();
		
		return true;
	}
	
	@Override
	public String toString() {
		String ret = version + "(" + (protocol == UDP?"UDP":(protocol == TCP?"TCP":protocol)) + ") > " + TCPIPUtils.ipAddressToString(sourceIp, 0) + ":" + sourcePort;
		ret = ret + " -> " + TCPIPUtils.ipAddressToString(destIp, 0) + ":" + destPort;
		ret = ret + " length : " + packetLength;
		return ret;
	}
	
	public int getBytes(byte[] buffer, int offset) {
		return -1;
	}
}
