package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.entity;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityPlayerMPTransformer;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

@Referenced(at = FrostEntityPlayerMPTransformer.class)
public interface FrostEntityPlayerMP extends IEntityPlayerMP, FrostEntity
{
    @Override
    default void sendBlock(int x, int y, int z)
    {
        sendPacket(new SPacketBlockChange(getWorld(), new BlockPos(x, y, z)));
    }

    @Override
    default void sendFakeBlock(int x, int y, int z, IState state)
    {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(8 + 4));
        buffer.writeBlockPos(new BlockPos(x, y, z));
        buffer.writeVarIntToBuffer(0);

        try
        {
            SPacketBlockChange packet = new SPacketBlockChange();
            packet.readPacketData(buffer);

            packet.blockState = (IBlockState) state;
            sendPacket(packet);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    default void sendTitle(MineCityForge mod, Message title, Message subtitle)
    {
        sendPacket(new SPacketTitle(SPacketTitle.Type.CLEAR, new TextComponentString("")));
        if(title != null)
        {
            sendPacket(new SPacketTitle(SPacketTitle.Type.TITLE, ITextComponent.Serializer.jsonToComponent(
                    mod.transformer.toJson(title)
            )));
        }
        else
        {
            sendPacket(new SPacketTitle(SPacketTitle.Type.TITLE, new TextComponentString("")));
        }

        if(subtitle != null)
        {
            sendPacket(new SPacketTitle(SPacketTitle.Type.SUBTITLE, ITextComponent.Serializer.jsonToComponent(
                    mod.transformer.toJson(subtitle)
            )));
        }
    }

    @NotNull
    @Override
    default String getName()
    {
        return IEntityPlayerMP.super.getName();
    }

    @Override
    default void sendTileEntity(int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = getWorld().getTileEntity(pos);
        if(tile == null)
            return;

        Packet packet = tile.getUpdatePacket();
        if(packet == null)
        {
            NBTTagCompound nbt = tile.serializeNBT();
            packet = new SPacketUpdateTileEntity(pos, 1, nbt);
        }

        sendPacket(packet);
    }
}
