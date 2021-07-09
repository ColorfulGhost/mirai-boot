package moe.iacg.miraiboot.plugins;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import moe.iacg.miraiboot.annotation.CommandPrefix;
import moe.iacg.miraiboot.enums.Commands;
import moe.iacg.miraiboot.uploader.OneDriveUploader;
import moe.iacg.miraiboot.utils.BotUtils;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static moe.iacg.miraiboot.constants.MsgTypeConstant.IMAGE;

@Slf4j
@Component
@CommandPrefix(command = Commands.UPLOAD)
public class Upload extends BotPlugin {
    @Autowired
    private BotUtils botUtils;

    @Autowired
    private OneDriveUploader oneDriveUploader;

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {
        Msg builder = Msg.builder();
        List<String> messageForType = BotUtils.getMessageForType(event.getMessageList(), IMAGE);
        List<File> images = new ArrayList<>();

        for (String url : messageForType) {
            File file = HttpUtil.downloadFileFromUrl(url, FileUtil.touch("images/tmpImage"));
            images.add(file);
        }

        try {
            oneDriveUploader.uploadFile(images.get(0), "jpg");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return botUtils.sendMessage(bot, event, builder);
    }
}
