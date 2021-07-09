package moe.iacg.miraiboot.plugins;

import cn.hutool.core.text.StrSplitter;
import lombok.extern.slf4j.Slf4j;
import moe.iacg.miraiboot.annotation.CommandPrefix;
import moe.iacg.miraiboot.entity.UserStatus;
import moe.iacg.miraiboot.enums.Commands;
import moe.iacg.miraiboot.service.UserStatusService;
import moe.iacg.miraiboot.utils.BotUtils;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@CommandPrefix(command = Commands.BGM)
public class Bangumi extends BotPlugin {

    @Autowired
    UserStatusService userStatusService;

    @Autowired
    BotUtils botUtils;

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event) {

        Msg messageBuilder = new Msg();
        long qq = event.getUserId();
        UserStatus UserStatus = userStatusService.get(qq);
        String acceptMessage = BotUtils.removeCommandPrefix(Commands.BGM.getCommand(), event.getRawMessage());

        if (StringUtils.isEmpty(acceptMessage)) {
            if (UserStatus == null) {
                UserStatus = new UserStatus();
                UserStatus.setQq(qq).setBangumiFlag(1);
                userStatusService.save(UserStatus);

                messageBuilder.text("你已经成功订阅新番更新提醒");
                return botUtils.sendMessage(bot, event, messageBuilder);
            }
            if (UserStatus.getBangumiFlag() == 1) {

                UserStatus.setQq(qq).setBangumiFlag(0);
                userStatusService.update(UserStatus);
                messageBuilder.text("你已经取消订阅新番更新提醒");
            } else {
                UserStatus.setQq(qq).setBangumiFlag(1);
                userStatusService.update(UserStatus);
                messageBuilder.text("再次订阅了新番更新提醒");
            }
            return botUtils.sendMessage(bot, event, messageBuilder);
        }

        if (acceptMessage.startsWith("rm ")) {
            List<String> bgmIds = StrSplitter.split(acceptMessage, " ", true, true);
            StringBuilder excludeBGMIds = new StringBuilder();

            for (int i = 1; i < bgmIds.size(); i++) {
                excludeBGMIds.append(bgmIds.get(i)).append(",");
            }

            String bangumiExclude = UserStatus.getBangumiExclude();
            if (StringUtils.isEmpty(bangumiExclude)) {
                UserStatus.setBangumiExclude(excludeBGMIds.toString());
                userStatusService.update(UserStatus);
            } else {
                UserStatus.setBangumiExclude(bangumiExclude + excludeBGMIds);
                userStatusService.update(UserStatus);
            }
            messageBuilder.text("已经移除番号：" + excludeBGMIds.toString() + "订阅。");
            return botUtils.sendMessage(bot, event, messageBuilder);
        }
        messageBuilder.text("你没有订阅或错误使用命令无法进行通知番剧过滤");
        return botUtils.sendMessage(bot, event, messageBuilder);

    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {

      return  botUtils.sendMessage(bot,event,Msg.builder().text("此功能作为私聊使用，请添加我为好友吧😜"));
    }
}
