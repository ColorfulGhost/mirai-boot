package moe.iacg.miraiboot.plugins;

import com.tekgator.queryminecraftserver.api.Protocol;
import com.tekgator.queryminecraftserver.api.QueryException;
import com.tekgator.queryminecraftserver.api.QueryStatus;
import com.tekgator.queryminecraftserver.api.Status;
import lombok.extern.slf4j.Slf4j;
import moe.iacg.miraiboot.annotation.CommandPrefix;
import moe.iacg.miraiboot.enums.Commands;
import moe.iacg.miraiboot.utils.BotUtils;
import moe.iacg.miraiboot.utils.RCONUtils;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@CommandPrefix(command = Commands.QMCS, alias = {"有人吗", "服里"})
public class QueryMCServer extends BotPlugin {
    @Autowired
    RCONUtils rconUtils;
    @Autowired
    BotUtils botUtils;

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {
        return botUtils.sendMessage(bot, event, retRCONCommand(event.getGroupId(), event.getRawMessage()));
    }


    private Msg retRCONCommand(Long qqGroupId, String rawMessage) {

        Msg builder = Msg.builder();

        String content = BotUtils.removeCommandPrefix(Commands.QMCS.getCommand(), rawMessage);

        String[] contentTexts = content.split(" ");

        String ip;
        int port = 25565;
        if (contentTexts.length == 0) {
            ip = "39.101.143.24";
        }else {
            ip = contentTexts[0];
            if (content.length()==2){
                port  = Integer.parseInt(contentTexts[1]);
            }
        }

        Status status = null;
        try {
            status = new QueryStatus.Builder(ip).setProtocol(Protocol.TCP)
                    .setPort(port)
                    .build()
                    .getStatus();
        } catch (QueryException e) {
            log.error(e.getMessage(), e);
        }

        if (status == null) {
            return builder;

        }
        Status.Players players = status.getPlayers();
        int onlinePlayers = players.getOnlinePlayers();
        builder.text("当前在线玩家：" + onlinePlayers + "人");

        Status.Version version = status.getVersion();
        String versionName = version.getName();


        return builder;
    }
}
