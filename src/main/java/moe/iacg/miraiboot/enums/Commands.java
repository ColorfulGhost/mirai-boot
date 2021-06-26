package moe.iacg.miraiboot.enums;

public enum Commands {
    /**
     * 命令全部用小写
     */
    YUANSHEN("ys", "原神相关命令\n" +
//            " - /ys bind 108288915 \n" +
//            " - /ys cookie ***** 【设置米游社Cookie后自动签到】\n" +
//            " - /ys sign 【手动签到】\n" +
//            " - /ys user 【查询角色信息】\n" +
//            " - /ys status 【查询自己的原神信息】\n" +
//            " - /ys status 108288915【查询他人的信息】\n" +
//            " - /ys status @yjx4【群内绑定过的好友信息】\n" +
//                    "/ys abyss 12-3【查询好友】\n" +
            " -/ys sp 32-80 【(当前体力)-(目标体力)】"
    ),
    HELP("help", "命令帮助"),
    DONATE("donate", "云要饭"),
    TOMP3("tomp3", "聊天语音转mp3"),
    IOT("iot", "基于mqtt的物联网功能 （老王自家使用）"),
    SWITCH("iot", "基于mqtt的物联网功能 （老王自家使用）"),

    BGM("bgm", "开启新番更新提醒(大陆未购买版权则不会提醒)，排除你不喜欢的新番如:/bgm rm 240835"),

    SETU("setu", "涩图！可加关键字，r18五五开检索。如：/setu yjx4"),

    AMQ("amq", "到点了，网抑云开启 (AMQ <anti-motivational quotes>)"),

    MCSM("mcsm", "二级命令 status、start_server、stop_server"),

    WATCH("watch", "谁在窥屏"),

    ANIME("anime", "以图搜番"),

    LIST("list", "查看在线玩家列表w"),

    MIBAND("miband", "查看老王今日运动数据"),

    LSP("lsp", "调用涩图API次数"),

    HITOKOTO("hitokoto", "一言"),

    RCON("rcon", "Minecraft服务端RCON命令（管理员权限）"),

    HPOOL("hpool", "矿池相关命令"),
    QMCS("qmcs", "查询MC服务器状态/qmcs mc.cn 25565"),
    //        STATISTIC("statistic", "获取玩家在线统计数据"),
    BIND("bind", "QQ绑定Minecraft账号。私聊我，如：/bind yjx4 password"),

    EDIT_PASSWORD("password", "修改Minecraft账户密码，私聊我，如：/password yjx4prprpr");
//    WORD("word", "填充语料库，正则测试网站：https://regexr.com/ ，正则表达式匹配(Java正则)。" +
//            "\n如精准匹配：/word exact yjx4のドレス何色なの？ 可愛いの色 " +
//            "\n如模糊匹配：/world fuzzy ドレス何色なの 可愛いの色" +
//            "\n如正则匹配：/world regex .*yjx4.* 萌え！！" +
//            "\n精准匹配>模糊匹配>正则匹配，如：Q: yjx4 A:萌え！！; Q:ドレス何色なの A:可愛いの色");
//    REPEAT("repeat", "人类的本质是复读机，所以我变的像人类了嘛？"),
//    TG("tg", "与Telegram上主人对话，如：/tg 主人，好梦~"),
//    LIKE("like", "小叽给您点赞啦~");


    private String command;
    private String description;

    Commands(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
