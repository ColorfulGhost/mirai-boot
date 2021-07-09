package moe.iacg.miraiboot.plugins;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import moe.iacg.miraiboot.constants.MsgTypeConstant;
import moe.iacg.miraiboot.utils.BotUtils;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotBase;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class AllFilterMsg extends BotPlugin {
    @Autowired
    private BotUtils botUtils;

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {


        Msg builder = Msg.builder();
        OnebotBase.Message message = event.getMessageList().get(0);
        if (message.getType().equals(MsgTypeConstant.LIGHT_APP)) {
            String content = message.getDataOrThrow("content");
            JSONObject mapMsg = JSONObject.parseObject(content);
            if (mapMsg.getString("app").equals("com.tencent.map")) {
                String address = mapMsg.getJSONObject("meta").getJSONObject("Location.Search").getString("address");
                builder.text(address);
                botUtils.sendMessage(bot, event, builder);
            }
        }
        return MESSAGE_IGNORE;
    }
}
