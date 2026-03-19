package net.cosette.columbina.network;

import net.cosette.columbina.Columbina;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// Packet S→C : ouvrir/mettre à jour le shop
public class ShopPayloads {
    // Payload envoyé serveur → client pour ouvrir le shop
    public record ShopOpenPayload(
            String shopId,
            boolean isBuy,
            int pageIndex,
            int playerPoints,
            // Items de la page : item id, prix, stock (-1=illimité)
            java.util.List<SlotData> slots
    ) implements CustomPayload {
        public static final Id<ShopOpenPayload> ID =
                new Id<>(Identifier.of(Columbina.MOD_ID, "shop_open"));
        public static final PacketCodec<PacketByteBuf, ShopOpenPayload> CODEC =
                PacketCodec.of(ShopOpenPayload::write, ShopOpenPayload::read);
        private void write(PacketByteBuf buf) {
            buf.writeString(shopId);
            buf.writeBoolean(isBuy);
            buf.writeInt(pageIndex);
            buf.writeInt(playerPoints);
            buf.writeInt(slots.size());
            for (SlotData s : slots) {
                buf.writeString(s.itemId());
                buf.writeInt(s.price());
                buf.writeInt(s.stock());
            }
        }
        private static ShopOpenPayload read(PacketByteBuf buf) {
            String shopId = buf.readString();
            boolean isBuy = buf.readBoolean();
            int pageIndex = buf.readInt();
            int points = buf.readInt();
            int count = buf.readInt();
            var slots = new java.util.ArrayList<SlotData>(count);
            for (int i = 0; i < count; i++) {
                slots.add(new SlotData(buf.readString(), buf.readInt(), buf.readInt()));
            }
            return new ShopOpenPayload(shopId, isBuy, pageIndex, points, slots);
        }
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
    // Payload envoyé client → serveur pour acheter/vendre
    public record ShopActionPayload(
            String shopId,
            boolean isBuy,
            int slotIndex,
            int quantity
    ) implements CustomPayload {
        public static final Id<ShopActionPayload> ID =
                new Id<>(Identifier.of(Columbina.MOD_ID, "shop_action"));
        public static final PacketCodec<PacketByteBuf, ShopActionPayload> CODEC =
                PacketCodec.of(ShopActionPayload::write, ShopActionPayload::read);
        private void write(PacketByteBuf buf) {
            buf.writeString(shopId);
            buf.writeBoolean(isBuy);
            buf.writeInt(slotIndex);
            buf.writeInt(quantity);
        }
        private static ShopActionPayload read(PacketByteBuf buf) {
            return new ShopActionPayload(
                    buf.readString(), buf.readBoolean(), buf.readInt(), buf.readInt());
        }
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
    public record SlotData(String itemId, int price, int stock) {}
}