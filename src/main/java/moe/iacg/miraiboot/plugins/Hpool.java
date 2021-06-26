package moe.iacg.miraiboot.plugins;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.extern.slf4j.Slf4j;
import moe.iacg.miraiboot.annotation.CommandPrefix;
import moe.iacg.miraiboot.entity.UserStatus;
import moe.iacg.miraiboot.enums.Commands;
import moe.iacg.miraiboot.model.HpoolMiningDetail;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpCookie;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@Component
@CommandPrefix(command = Commands.HPOOL)
public class Hpool extends BotPlugin {

    private static String hpoolApi = "https://hpool.co/api/pool/miningdetail?language=zh&type=chia&count=100&page=";

    //    private static final String xchPriceChart = "https://api2.chiaexplorer.com/chart/xchPriceChart?period=24h";
    private static final String xchPriceChartForOKEX = "https://www.okex.com/v2/spot/instruments/XCH-USDT/candles?size=1&granularity=20";

    private static final String lastMiningInComeRecord = "https://hpool.co/api/pool/miningincomerecord?language=zh&type=chia&count=1&page=1";
    private static final String totalAssets = "https://hpool.co/api/assets/totalassets";
    @NacosValue("${hpool.cookie}")
    private String selfHpoolCookie;

    @Autowired
    private UserStatusService userStatusService;

    @Autowired
    BotUtils botUtils;

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event) {

        String cookie = BotUtils.removeCommandPrefix(Commands.HPOOL.getCommand(), event.getRawMessage());

        Msg builder = Msg.builder();
        long userId = event.getUserId();
        UserStatus userStatus = userStatusService.get(userId);

        if (StringUtils.isEmpty(cookie)) {

            if (userStatus==null||StringUtils.isEmpty(userStatus.getHpoolCookie())) {
                builder.text("您未设置Hpool Cookie");
                return botUtils.sendMessage(bot, event, builder);
            }

            Msg hpoolDataMsg = getHpoolDataMsg(userStatus.getHpoolCookie());
            return botUtils.sendMessage(bot, event, hpoolDataMsg);

        } else {

            if (userStatus == null) {
                userStatus = new UserStatus();
                userStatus.setHpoolCookie(cookie);
                userStatus.setQq(userId);
            } else {
                userStatus =userStatusService.get(userId);
                userStatus.setHpoolCookie(cookie);
            }
            userStatusService.save(userStatus);

            builder.text("你已经成功设置Hpool Cookie");
        }

        return botUtils.sendMessage(bot, event, builder);
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {

        Msg builder = getHpoolDataMsg(null);

        return botUtils.sendMessage(bot, event, builder);
    }


    private Msg getHpoolDataMsg(String cookie) {
        if (cookie == null) {
            cookie = selfHpoolCookie;
        }
        int page = 1;
        List<HpoolMiningDetail.Data.Settlement> allData = new ArrayList<>();
        Integer total = getPoolMinigDetail(page, allData, cookie);

        int pages = (total + 100 - 1) / 100;
        if (pages > 1) {
            for (int i = 2; i <= pages; i++) {
                getPoolMinigDetail(i, allData, cookie);
            }
        }
        BigDecimal unSettlement = BigDecimal.ZERO;
        BigDecimal settlements = BigDecimal.ZERO;
        for (HpoolMiningDetail.Data.Settlement allDatum : allData) {
            if (allDatum.getStatus() == 0) {
                unSettlement = unSettlement.add(allDatum.getBlock_reward());
            }
            if (allDatum.getStatus() == 2) {
                settlements = settlements.add(allDatum.getBlock_reward());
            }

        }
        BigDecimal count = settlements.add(unSettlement);

        JSONObject lastMiningInComeRecord = lastMiningInComeRecord(cookie);

        Msg builder = Msg.builder();
        builder.text("哈池CHIA结算情况统计：\n");
        builder.text("最近一次结算时间：").text(convertTime(lastMiningInComeRecord.getDate("record_time").getTime() * 1000))
                .text("--XCH：").text(lastMiningInComeRecord.getString("amount")).text("\n");
        builder.text("当前可提现余额：").text(totalAssets(cookie)).text("\n");
        builder.text("当日未结算XCH：" + unSettlement.divide(new BigDecimal(2), 8, RoundingMode.HALF_EVEN)).text("\n");
        builder.text("已结算XCH：" + settlements.divide(new BigDecimal(2), 8, RoundingMode.HALF_EVEN)).text("\n");
        builder.text("矿池总收益XCH：" + count.divide(new BigDecimal(2), 8, RoundingMode.HALF_EVEN)).text("\n");
        builder.text("OKEX实时XCH价格：" + currentXCHPriceChart().divide(new BigDecimal(1), 2, RoundingMode.HALF_EVEN)).text("\n");
        builder.text("矿池总收益USDT：" + count.multiply(currentXCHPriceChart()).divide(new BigDecimal(2), 8, RoundingMode.HALF_EVEN)).text("\n");
        return builder;
    }

    private JSONObject lastMiningInComeRecord(String cookie) {
        HttpRequest request = HttpUtil.createGet(lastMiningInComeRecord);
        request.cookie(new HttpCookie("auth_token", cookie));
        HttpResponse execute = request.execute();
        String data = execute.body();

        JSONObject jsonObject = JSON.parseObject(data)
                .getJSONObject("data").getJSONArray("list").getJSONObject(0);

        return jsonObject;

    }

    private String totalAssets(String cookie){
        HttpRequest request = HttpUtil.createGet(totalAssets);
        request.cookie(new HttpCookie("auth_token", cookie));
        HttpResponse execute = request.execute();
        String data = execute.body();
        JSONObject jsonObject = JSON.parseObject(data)
                .getJSONObject("data").getJSONArray("list").getJSONObject(1);

        return jsonObject.getString("balance");

    }

    public String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    private BigDecimal currentXCHPriceChart() {
        String data = HttpUtil.get(xchPriceChartForOKEX);

        JSONArray priceList = JSON.parseObject(data).getJSONArray("data");

        String price = (String) ((JSONArray) priceList.get(0)).get(1);
        return new BigDecimal(price);
    }

    private Integer getPoolMinigDetail(int page, List<HpoolMiningDetail.Data.Settlement> allData, String cookie) {
        HttpRequest request = HttpUtil.createGet(hpoolApi + page);
        request.cookie(new HttpCookie("auth_token", cookie));
        HttpResponse execute = request.execute();
        String body = execute.body();

        HpoolMiningDetail hpoolMiningDetail = JSONObject.parseObject(body, HpoolMiningDetail.class);
        HpoolMiningDetail.Data data = hpoolMiningDetail.getData();


        Integer total = data.getTotal();
        allData.addAll(data.getList());
        return total;
    }
}