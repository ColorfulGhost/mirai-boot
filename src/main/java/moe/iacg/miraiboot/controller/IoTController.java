package moe.iacg.miraiboot.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/IoT")
@Slf4j
public class IoTController {

//    @Autowired
//    MqttGatewayApi mqttGatewayApi;

    private final static String goveeBaseApi = "https://developer-api.govee.com/v1/devices/";

    private final String APIKEY_CONSTANT = "Govee-API-Key";

    @NacosValue("${govee.api.key}")
    private String goveeApiKey;

    @RequestMapping("/devices")
    public String devices() {

        String body = HttpUtil.createGet(goveeBaseApi)
                .header(APIKEY_CONSTANT, goveeApiKey)
                .execute()
                .body();
        log.info(body);
        return body;
    }
}