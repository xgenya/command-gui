package com.remrin.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * S2C payload carrying the server's command GUI presets to clients.
 * <p>
 * Defined in the COMMON source set of easy-cmd so that eun_carpet can depend on this class
 * as a {@code modCompileOnly} reference and send typed instances of it.
 * <p>
 * Identifier: {@code eun_carpet:cmd_gui_preset}
 */
public record ServerCommandPayload(List<ServerCommandGroup> groups) implements CustomPacketPayload {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("eun_carpet", "cmd_gui_preset");
	public static final Type<ServerCommandPayload> TYPE = new Type<>(ID);

	public static final StreamCodec<FriendlyByteBuf, ServerCommandPayload> CODEC = new StreamCodec<>() {
		@Override
		public void encode(FriendlyByteBuf buf, ServerCommandPayload payload) {
			buf.writeVarInt(payload.groups().size());
			for (ServerCommandGroup group : payload.groups()) {
				buf.writeUtf(group.getName());
				List<ServerCommandEntry> commands = group.getCommands();
				buf.writeVarInt(commands == null ? 0 : commands.size());
				if (commands != null) {
					for (ServerCommandEntry entry : commands) {
						buf.writeUtf(entry.getName());
						buf.writeUtf(entry.getDescription());
						List<String> cmds = entry.getCommands();
						buf.writeVarInt(cmds == null ? 0 : cmds.size());
						if (cmds != null) {
							for (String cmd : cmds) buf.writeUtf(cmd);
						}
					}
				}
			}
		}

		@Override
		public ServerCommandPayload decode(FriendlyByteBuf buf) {
			int groupCount = buf.readVarInt();
			List<ServerCommandGroup> groups = new ArrayList<>(groupCount);
			for (int i = 0; i < groupCount; i++) {
				String groupName = buf.readUtf();
				int entryCount = buf.readVarInt();
				List<ServerCommandEntry> entries = new ArrayList<>(entryCount);
				for (int j = 0; j < entryCount; j++) {
					String name = buf.readUtf();
					String description = buf.readUtf();
					int cmdCount = buf.readVarInt();
					List<String> cmds = new ArrayList<>(cmdCount);
					for (int k = 0; k < cmdCount; k++) cmds.add(buf.readUtf());
					entries.add(new ServerCommandEntry(name, cmds, description));
				}
				groups.add(new ServerCommandGroup(groupName, entries));
			}
			return new ServerCommandPayload(groups);
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
