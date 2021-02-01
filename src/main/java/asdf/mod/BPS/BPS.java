/*
 * Client Side Block Placing Sound Mod
 * By asdfcube
 * */

package asdf.mod.BPS;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import tv.twitch.chat.Chat;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(modid="asdfbps",version="1",clientSideOnly=true)
public class BPS{
    private static boolean enabled=true;
    // Switched from CopyOnWriteArrayList to ArrayList for performance even exceptions might occur in rare cases
    private final ArrayList<String> blocks=new ArrayList<>();
    private boolean inited=false;
    private static boolean local;
    // Multithreading B)
    private final ExecutorService executor=Executors.newFixedThreadPool(15);

    private long ping;

    // Initialize the mod, which only consists of a few event listeners and one singular command
    @EventHandler
    public void init(FMLInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CMD());
    }

    // Command (/bps)
    private static class CMD extends CommandBase implements ICommand{
        @Override
        public String getCommandName(){return "bps";}
        @Override
        public String getCommandUsage(ICommandSender iCommandSender){return "/bps";}
        @Override
        public boolean canCommandSenderUseCommand(ICommandSender iCommandSender){return true;}
        @Override
        public void processCommand(ICommandSender iCommandSender,String[] strings) throws CommandException{
            if(strings.length==0){
                if(enabled && !local) iCommandSender.addChatMessage(new ChatComponentText("BPS is currently enabled. Do \"/bps toggle\" to disable it."));
                else iCommandSender.addChatMessage(new ChatComponentText("BPS is currently disabled. Do \"/bps toggle\" to enable it."));
            }else if(strings[0].equals("toggle")){
                if(local) iCommandSender.addChatMessage(new ChatComponentText("You don't need it on a local server."));
                else{
                    if(enabled) iCommandSender.addChatMessage(new ChatComponentText("BPS has been disabled, log out of current server for the toggle to take effect."));
                    else iCommandSender.addChatMessage(new ChatComponentText("BPS has been enabled, log out of current server for the toggle to take effect."));
                    enabled=!enabled;
                }
            }else iCommandSender.addChatMessage(new ChatComponentText("Unknown command, do \"/bps\" to check current toggle state, \"/bps toggle\" to toggle it."));
        }
    }

    // Triggered when you join a server to initialize the packet handlers
    @SubscribeEvent
    public void ServerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event){
        // Check if BPS is enabled
        local=event.isLocal;
        if(enabled){
            // You don't need this on local server
            if(!local){
                blocks.clear();
                // Create a netty pipeline handler
                ChannelDuplexHandler handlerIn=new ChannelDuplexHandler(){
                    @Override
                    public void channelRead(ChannelHandlerContext context,Object packet) throws Exception{
                        // Ignore S29PacketSoundEffect of those blocks you placed
                        try{
                            if(packet instanceof S29PacketSoundEffect &&
                                    // ((S29PacketSoundEffect)packet).getPitch()==0.7936508f &&
                                    blocks.contains(((S29PacketSoundEffect)packet).getX()+" "+
                                            ((S29PacketSoundEffect)packet).getY()+" "+
                                            ((S29PacketSoundEffect)packet).getZ())
                            ) ;
                            else super.channelRead(context,packet);
                        }catch(ConcurrentModificationException e){
                            super.channelRead(context,packet);
                        }
                    }
                };
                ChannelDuplexHandler handlerOut=new ChannelDuplexHandler(){
                    @Override
                    public void write(ChannelHandlerContext context,Object packet,ChannelPromise channelPromise) throws Exception{
                        super.write(context,packet,channelPromise);
                        // Use block placing packet instead of event manager because latter sucks
                        if(packet instanceof C08PacketPlayerBlockPlacement &&
                                !Minecraft.getMinecraft().playerController.getCurrentGameType().isAdventure()){
                            executor.execute(()->{
                                C08PacketPlayerBlockPlacement blockpacket=(C08PacketPlayerBlockPlacement)packet;
                                if(// Block direction=255 means nothing is placed
                                        blockpacket.getPlacedBlockDirection()!=255 &&
                                        blockpacket.getStack()!=null &&
                                        blockpacket.getStack().getItem() instanceof ItemBlock){
                                    boolean notthisblock=true;
                                    switch(Block.getIdFromBlock(Minecraft.getMinecraft().theWorld.getBlockState(blockpacket.getPosition()).getBlock())){
                                        // Some blocks are usable on rightclick
                                        case 23: case 25: case 26: case 36: case 54: case 61: case 62: case 63: case 64: case 68: case 58: case 69: case 71: case 77: case 84: case 85: case 92: case 93: case 94: case 96: case 107: case 113: case 116: case 117: case 118: case 122: case 130: case 137: case 138: case 140: case 143: case 145: case 146: case 149: case 150: case 151: case 154: case 167: case 178: case 183: case 184: case 185: case 186: case 187: case 188: case 189: case 190: case 191: case 192: case 193: case 194: case 195: case 196:
                                            if(!Minecraft.getMinecraft().thePlayer.isSneaking()) return;
                                            break;
                                        // Some blocks are just replaced if you right click on them
                                        case 6: case 8: case 9: case 10: case 11: case 31: case 32: case 78: case 106:
                                            notthisblock=false;
                                    }
                                    BlockPos blockpos=blockpacket.getPosition();
                                    float x=(float)(blockpos.getX()+0.5);
                                    float y=(float)(blockpos.getY()+0.5);
                                    float z=(float)(blockpos.getZ()+0.5);
                                    if(notthisblock){
                                        switch(blockpacket.getPlacedBlockDirection()){
                                            case 0: y--;break;
                                            case 1: y++;break;
                                            case 2: z--;break;
                                            case 3: z++;break;
                                            case 4: x--;break;
                                            case 5: x++;
                                        }
                                    }
                                    float finalX=x;float finalY=y;float finalZ=z;
                                    // Schedule the sound in main thread
                                    Minecraft.getMinecraft().addScheduledTask(()->Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(((ItemBlock)blockpacket.getStack().getItem()).getBlock().stepSound.getPlaceSound()),
                                            // The pitch of block placing is 0.7936508, don't question it
                                            1f,0.7936508f,finalX,finalY,finalZ)));
                                    String c=x+" "+y+" "+z;
                                    blocks.add(c);
                                    // Delete the data after 600ms
                                    try{
                                        Thread.sleep(ping);
                                        blocks.remove(c);
                                    // Catching ConcurrentModificationException
                                    }catch(Exception e){}
                                }
                            });
                        }
                    }
                };
                // Register the handler before the Minecraft handler so that some packets can be ignored
                event.manager.channel().pipeline().addBefore("packet_handler","asdfInHandler",handlerIn);
                // Register the handler after the Minecraft handler to play sound
                event.manager.channel().pipeline().addAfter("packet_handler","asdfOutHandler",handlerOut);
                inited=true;
                ping=Minecraft.getMinecraft().getCurrentServerData().pingToServer+150;
            }else{
                executor.execute(()->{
                    while(Minecraft.getMinecraft().thePlayer==null) try{Thread.sleep(500);}catch(InterruptedException e){}
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("BPS is automatically disabled on local servers."));
                });
            }
        }else{
            executor.execute(()->{
                while(Minecraft.getMinecraft().thePlayer==null) try{Thread.sleep(500);}catch(InterruptedException e){}
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("BPS was toggled off, do \"/bps toggle\" to toggle it back on."));
            });
        }
    }

    // Triggered when you leave a server to remove the packet handler
    @SubscribeEvent
    public void ServerQuitEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event){
        if(inited){
            inited=false;
            Channel channel=event.manager.channel();
            channel.eventLoop().submit(()->{
                channel.pipeline().remove("asdfInHandler");
                channel.pipeline().remove("asdfOutHandler");
                return null;
            });
        }
    }

    // That's it, simple, ez
}
