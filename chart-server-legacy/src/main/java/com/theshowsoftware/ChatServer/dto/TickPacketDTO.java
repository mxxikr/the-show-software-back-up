package com.theshowsoftware.ChatServer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.nio.ByteBuffer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickPacketDTO {
    private BigInteger price;                 // 가격
    private Long quantity;                // 거래량
    private Long timestamp;             // 수집 시간

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putDouble(price.doubleValue());
        buffer.putLong(quantity);
        return buffer.array();
    }

    public static TickPacketDTO fromBytes(ByteBuffer buffer) {
        TickPacketDTO packet = new TickPacketDTO();
        packet.price = BigInteger.valueOf((long) buffer.getDouble());
        packet.quantity = buffer.getLong();
        return packet;
    }
}
