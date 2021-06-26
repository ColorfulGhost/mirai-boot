package moe.iacg.miraiboot.plugins;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import moe.iacg.miraiboot.annotation.CommandPrefix;
import moe.iacg.miraiboot.enums.Commands;
import moe.iacg.miraiboot.limit.RedisRaterLimiter;
import moe.iacg.miraiboot.model.SeTuResponseModel;
import moe.iacg.miraiboot.utils.BotUtils;
import moe.iacg.miraiboot.utils.RedisUtil;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Ghost
 */
@Slf4j
@Component
@CommandPrefix(command = Commands.SETU, alias = {"开车", "涩图", "色图", "营养", "开冲", "够色", "摩多", "兴奋", "黄图"})
public class SeTu extends BotPlugin {

    @Autowired
    RedisUtil redisUtil;
    private int tmpNextKeyIndex = 0;

    @Autowired
    RedisRaterLimiter redisRaterLimiter;

    @NacosValue("${setu.api.key}")
    private String seTuAPIKeys;

    @NacosValue("${lolicon.proxy.url}")
    private String loliconProxyURL;

    @NacosValue("${setu.auth.groups}")
    private String seTuAuthGroups;

    @Autowired
    private BotUtils botUtils;
    public static final String BOT_SETU_COUNT = "bot:setu:count";


    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {
        String[] groups = seTuAuthGroups.split(",");
        boolean hasGroup = Arrays.stream(groups).anyMatch(group -> group.equals(String.valueOf(event.getGroupId())));
        if (hasGroup) {
            return botUtils.sendMessage(bot, event, sendSeTu(event.getRawMessage()));

        } else {
            return botUtils.sendMessage(bot, event, Msg.builder().text("您的QQ群不能开车，请找管理员考取驾照吧"));
        }

    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event) {
        return botUtils.sendMessage(bot, event, sendSeTu(event.getRawMessage()));
    }

    @SneakyThrows
    private Msg sendSeTu(String message) {


        String keyword = BotUtils.removeCommandPrefix(Commands.SETU.getCommand(), message);
        Msg builder = Msg.builder();

        for (String alias : this.getClass().getAnnotation(CommandPrefix.class).alias()) {
            if (message.contains(alias)) {
                keyword = null;
                break;
            }
        }


        Result parse = ToAnalysis.parse(message);
        String chineseNumber = ReUtil.get(",(.*)\\/mq", parse.toString(), 1);
        List<String> keywords = parse.getTerms()
                .stream().map(Term::toString)
                .filter(str -> str.contains("/n") || str.contains("/en"))
                .collect(Collectors.toList());

        if (StringUtils.isNotEmpty(chineseNumber)) {
            chineseNumber = chineseNumber.substring(0, chineseNumber.length() - 1);
            int number;
            try {
                number = Integer.parseInt(chineseNumber);
            } catch (NumberFormatException e) {
                number = BotUtils.zh2arbaNum(chineseNumber);
            }

            if (number > 11) {
                builder.text("差不多得了").face(1);
                return builder;
            }

            return batchGetSeTuApi(number);

        } else {
            if (keywords.size() > 1) {
                keyword = keywords.get(1).split("/")[0];
            }
        }

        if ("count".equals(keyword)) {
            String setuCount = redisUtil.get(BOT_SETU_COUNT);
            return builder.text("今天咱发了").text(setuCount).text("份色图，少看涩图，一分钟不超过五张，手离几把越近，健康离你越远。");
        }

        String seTuApi = getSeTuApi(keyword, 1);
        if (seTuApi.contains("null")) {
            builder.text("没有这种涩图，可能你的性癖太怪了:P");
            return builder;
        }
        builder.image(seTuApi);
        return builder;
    }

    /**
     * lolicon_proxy
     *
     * @param keyword
     * @param r18
     * @return
     */
    public String getSeTuApi(String keyword, int r18) {
        setuCount();
        Map<String, Object> requestData = new HashMap<>();
        if (!StringUtils.isEmpty(keyword)) {
            requestData.put("keyword", keyword);
        }
        requestData.put("r18", r18);

        var result = loliconProxyURL + ReUtil.get("href=\"(.*)\">",
                HttpUtil.get(loliconProxyURL + "/lolicon", requestData), 1);
        return result;
    }

    @SneakyThrows
    public Msg batchGetSeTuApi(int size) {
        Msg builder = Msg.builder();
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CompletableFuture<String> seTuFuture = CompletableFuture.supplyAsync(() -> getSeTuApi(null, 0));
            futures.add(seTuFuture);
        }
        CompletableFuture<Void> cfFetch = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));

        CompletableFuture<List<String>> listCompletableFuture = cfFetch.thenApply(future ->
                futures.stream()
                        .map(completableFuture -> completableFuture.join()).collect(Collectors.toList()));

        for (String url : listCompletableFuture.get()) {
            builder.image(url);
        }
        return builder;
    }

    /**
     * 文档：https://api.lolicon.app/
     *
     * @param keyword 若指定关键字，将会返回从插画标题、作者、标签中模糊搜索的结果
     * @param r18     0为非 R18，1为 R18，2为混合
     * @param num     一次返回的结果数量，范围为1到10，不提供 APIKEY 时固定为1；在指定关键字的情况下，结果数量可能会不足指定的数量
     * @return
     */


    public SeTuResponseModel seTuApi(String keyword, int r18, int num) {
        Map<String, Object> requestData = new HashMap<>();
        while (true) {
            List<String> seTuKeys = Arrays.asList(seTuAPIKeys.split(","));
            if (tmpNextKeyIndex < seTuKeys.size()) {
                requestData.put("apikey", seTuKeys.get(tmpNextKeyIndex));
                tmpNextKeyIndex++;
                break;
            } else {
                tmpNextKeyIndex = 0;
            }
        }
        if (!StringUtils.isEmpty(keyword)) {
            requestData.put("keyword", keyword);
        }
        requestData.put("r18", r18);
        requestData.put("num", num);
        requestData.put("proxy", "i.pixiv.cat");
        requestData.put("size1200", true);
        String jsonData;
        SeTuResponseModel seTuResponseModel;

        jsonData = HttpUtil.get("https://api.lolicon.app/setu/", requestData);
        seTuResponseModel = JSON.parseObject(jsonData, SeTuResponseModel.class);

        setuCount();

        return seTuResponseModel;
    }

    private void setuCount() {
        redisUtil.incrBy(BOT_SETU_COUNT, 1);

        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 23);
        instance.set(Calendar.MINUTE, 59);
        instance.set(Calendar.SECOND, 58);
        redisUtil.expireAt(BOT_SETU_COUNT, instance.getTime());
    }


}
