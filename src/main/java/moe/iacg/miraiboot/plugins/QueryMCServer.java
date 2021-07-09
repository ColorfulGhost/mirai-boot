package moe.iacg.miraiboot.plugins;

import cn.hutool.core.util.ArrayUtil;
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
import org.springframework.util.StringUtils;

@Slf4j
@Component
@CommandPrefix(command = Commands.MCSTATUS, alias = {"有人吗", "服里"})
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

        String content = BotUtils.removeCommandPrefix(Commands.MCSTATUS.getCommand(), rawMessage);

        String[] contentTexts = content.split(" ");
        int port = 25565;
        String ip;
        if (StringUtils.isEmpty(content)) {
            ip = "39.101.143.24";

        } else {
            ip = contentTexts[0];
            if (content.length() == 2) {
                port = Integer.parseInt(contentTexts[1]);
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
        Status.Players.Player[] playerEntry = players.getPlayer();
        if (onlinePlayers == 0) {
            builder.text("当前无玩家在线。");
            return builder;
        }
        builder.text("当前在线玩家：" + onlinePlayers + "人\n");

        if (ArrayUtil.isNotEmpty(playerEntry)) {
            for (Status.Players.Player player : playerEntry) {
                builder.text(player.getName() + "\n");
            }
        }
        Status.Version version = status.getVersion();
        builder.text("当前服务端版本：" + version.getName());

        return builder;
    }
}
