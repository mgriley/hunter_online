package application.shared.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import application.server.GameObject;
import application.shared.states.BulletState;
import application.shared.states.EntityState;
import application.shared.states.HunterState;

public class StateUpdatePacket implements Packet {
	
	// True if the hunter of the client to which this packet is addressed is alive, false ow
	private boolean hunterAlive;
	private float globalCooldown;
	private int equippedId;
	private List<EntityState> states;
	
	private StateUpdatePacket() {}
	
	public static StateUpdatePacket toProcess() {
		StateUpdatePacket p = new StateUpdatePacket();
		return p;
	}
	
	private List<GameObject> objects;
	
	public static StateUpdatePacket toSend(
			boolean hunterAlive,
			List<GameObject> objects,
			float globalCooldown, int equippedId) {
		StateUpdatePacket p = new StateUpdatePacket();
		p.hunterAlive = hunterAlive;
		p.objects = objects;
		p.globalCooldown = globalCooldown;
		p.equippedId = equippedId;
		return p;
	}
	
	@Override
	public boolean process(DatagramPacket packet, MessageUnpacker unpacker) {
		
		try {
			hunterAlive = unpacker.unpackBoolean();
			globalCooldown = unpacker.unpackFloat();
			equippedId = unpacker.unpackInt();
			
			int numEntities = unpacker.unpackInt();
			states = new LinkedList<EntityState>();
			for (int i = 0; i < numEntities; i++) {
				int typeId = unpacker.unpackByte();
				
				// Validate the id
				if (!(0 <= typeId && typeId < EntityState.Type.values().length)) {
					System.out.println(typeId + " is an invalid type id");
					return false;
				}
				
				// Reconstruct the entity from the data
				EntityState.Type type = EntityState.Type.values()[typeId];
				EntityState state = null;
				switch (type) {
					case HUNTER:
						state = new HunterState();
						break;
					case BULLET:
						state = new BulletState();
						break;
				}
				state.read(unpacker);
				states.add(state);
			}
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public void send(DatagramSocket socket, SocketAddress address) throws IOException {
		
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		packer.packByte(Packet.Type.STATEUPDATE.getID());
		
		// Pack player-specific variables
		packer.packBoolean(hunterAlive);
		packer.packFloat(globalCooldown);
		packer.packInt(equippedId);
		
		// Package the game objects
		
		int size = objects.size();
		packer.packInt(size);
		
		// Pack each game object
		Iterator<GameObject> it = objects.iterator();
		int i = 0;
		while (it.hasNext() && i < size) {
			EntityState s = it.next().getState();
			s.write(packer);
			i++;
		}
		
		packer.flush();
		byte[] buf = packer.toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
		socket.send(packet);
	}
	
	public boolean isHunterAlive() {
		return hunterAlive;
	}
	
	public List<EntityState> getStates() {
		return states;
	}
	
	public int getEquippedId() {
		return equippedId;
	}
	
	public float getGlobalCooldown() {
		return globalCooldown;
	}
}