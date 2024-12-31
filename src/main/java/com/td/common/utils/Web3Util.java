package com.td.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.td.client.controller.TransactionController;
import com.td.client.enums.RedPacketConfigKeyEnums;
import com.td.client.service.RedPacketConfigService;
import com.td.common.exception.CustomException;
import com.td.common.lock.CustomRedisLock;
import com.td.common.pojo.TransactionRecords;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class Web3Util {

    private static final String INFURA_URL = "/api/transactionCount"; // 获取交易计数和符号的接口
    private static final String RAW_TX_URL = "/api/rawTransaction"; // 发送原始交易的接口
    private static final String OLINK_CALL_URL = "/api/olinkCall"; // 获取余额的接口
    private static final String GAS_LIMIT_URL = "/api/gasLimit"; // 获取 Gas 限制的接口
    private static final String GET_TX_INFO = "/api/tx_info?txid="; //获取交易信息
    private static final String COIN_ADDRESS = "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5"; // 合约地址


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedPacketConfigService redPacketConfigService;

    @Autowired
    private CustomRedisLock customRedisLock;

    @Autowired
    private MongoTemplate mongoTemplate;

    // 获取交易计数和符号
    public Map<String, String> getTransactionCountAndSymbol(String address) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("addr", address);

            ResponseEntity<Map> response = restTemplate.getForEntity(INFURA_URL, Map.class, params);

            String result = (String) response.getBody().get("result");
            String extra = (String) response.getBody().get("extra");

            Map<String, String> countAndSymbol = new HashMap<>();
            countAndSymbol.put("nonce", result);
            countAndSymbol.put("symbol", extra);

            return countAndSymbol;
        } catch (Exception e) {
            System.err.println("Web3Util-getTransactionCountAndSymbol error = " + e.toString());
            return null;
        }
    }

    /**
     * @param fromAddress 发送地址
     * @param toAddress   接收地址
     * @param privKey     私钥
     * @param amount      金额
     * @return
     */
    public String transfer(String fromAddress, String toAddress, String privKey, BigDecimal amount) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("金额必须大于0");
            }
            //使用restTemplate调用接口
            String res = restTemplate.getForObject(INFURA_URL + "?addr=" + fromAddress, String.class);
            JSONObject jsonRes = JSON.parseObject(res);
            /**
             * {"msg":"ok","result":"0x0","code":0,"extra":"983b3b5cb95d7efc9190be44c824c51e"}
             */
            log.info("获取地址：{} nonce 响应:{}", fromAddress, jsonRes);
            String code = jsonRes.getString("code");
            if (!"0".equals(code) || "".equals(jsonRes.getString("nonce"))) {
                throw new CustomException("获取nonce失败");
            }

            BigInteger nonce = new BigInteger(jsonRes.getString("result").replace("0x", ""), 16);
            String symbol = jsonRes.getString("extra");

            BigInteger amountWei = toWei(amount.toString());
            //转为16进制
            String amountHax = Numeric.toHexString(amountWei.toByteArray());

            BigInteger gasLimit = getGasLimit(fromAddress, toAddress, amountHax, "0x");

            BigInteger gasPrice = Convert.toWei("2", Convert.Unit.GWEI).toBigInteger();


            // 创建原始交易
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, gasPrice, gasLimit, toAddress, amountWei);

            // 用私钥签名交易
            Credentials credentials = Credentials.create(privKey);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, 708, credentials);
            String rawTx = Numeric.toHexString(signedMessage);


            // 准备POST请求数据
            MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();
            postData.add("raw", rawTx);
            postData.add("symbol", symbol);
            postData.add("version", "999999");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(postData, headers);


            String response = restTemplate.postForEntity(RAW_TX_URL, request, String.class).getBody();
            // 输出响应
            /**
             * {"code":0,"msg":"ok","result":{"result":"0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501","error":null},"error":null}
             */

            JSONObject jsonResponse = JSON.parseObject(response);
            String codeResponse = jsonResponse.getString("code");
            if (!"0".equals(codeResponse) || "".equals(jsonResponse.getString("result"))) {
                throw new CustomException("充值失败");
            }

            JSONObject result = jsonResponse.getJSONObject("result");
            return result.getString("result");
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常
            return null; // 返回 null
        }
    }

    public static byte[] hexToByteArray(String hexString) {
        if (hexString.startsWith("0x")) {
            hexString = hexString.substring(2);
        }

        // 验证字符串长度是否为偶数
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even length");
        }

        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < hexString.length(); i += 2) {
            String byteString = hexString.substring(i, i + 2);
            byte byteValue = (byte) Integer.parseInt(byteString, 16);
            byteList.add(byteValue);
        }

        // 将 List<Byte> 转换为 byte[]
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }

        return byteArray;
    }

    public String transferOWL(String fromAddress, String toAddress, String privKey, BigDecimal amount) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("金额必须大于0");
            }
            //使用restTemplate调用接口
            String res = restTemplate.getForObject(INFURA_URL + "?addr=" + fromAddress, String.class);
            JSONObject jsonRes = JSON.parseObject(res);
            /**
             * {"msg":"ok","result":"0x0","code":0,"extra":"983b3b5cb95d7efc9190be44c824c51e"}
             */
            log.info("获取地址：{} nonce 响应:{}", fromAddress, jsonRes);
            String code = jsonRes.getString("code");
            if (!"0".equals(code) || "".equals(jsonRes.getString("nonce"))) {
                throw new CustomException("获取nonce失败");
            }


            BigInteger nonce = new BigInteger(jsonRes.getString("result").replace("0x", ""), 16);
            String symbol = jsonRes.getString("extra");

            BigInteger amountWei = toWei(amount.toString());
            //转为16进制

            String dataSend = genErc20Transfer(toAddress, amountWei);

            BigInteger gasLimit = getGasLimit(fromAddress, COIN_ADDRESS, "0x0", dataSend);

            BigInteger gasPrice = Convert.toWei("2", Convert.Unit.GWEI).toBigInteger();

            RawTransaction transaction = RawTransaction.createTransaction(
                    nonce, gasPrice, gasLimit, COIN_ADDRESS, BigInteger.ZERO, dataSend
            );


            // 用私钥签名交易
            Credentials credentials = Credentials.create(privKey);
            byte[] signedMessage = TransactionEncoder.signMessage(transaction, 708, credentials);
            String rawTx = Numeric.toHexString(signedMessage);

            // 准备POST请求数据
            MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();
            postData.add("raw", rawTx);
            postData.add("symbol", symbol);
            postData.add("version", "999999");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(postData, headers);


            String response = restTemplate.postForEntity(RAW_TX_URL, request, String.class).getBody();
            // 输出响应
            /**
             * {"code":0,"msg":"ok","result":{"result":"0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501","error":null},"error":null}
             */

            System.out.println(response);
            JSONObject jsonResponse = JSON.parseObject(response);
            String codeResponse = jsonResponse.getString("code");
            if (!"0".equals(codeResponse) || "".equals(jsonResponse.getString("result"))) {
                throw new CustomException("充值失败");
            }

            JSONObject result = jsonResponse.getJSONObject("result");
            return result.getString("result");
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常
            return null; // 返回 null
        }
    }

    public String withdrawOWL(String toAddress, Double amount) {
        String fromAddress = redPacketConfigService.getConfig(RedPacketConfigKeyEnums.CENTER_SERVER_ADDRESS.getKey());
        String privateKey = redPacketConfigService.getConfig(RedPacketConfigKeyEnums.CENTER_SERVER_PRIVATE_KEY.getKey());

        //使用restTemplate调用接口
        String res = restTemplate.getForObject(INFURA_URL + "?addr=" + fromAddress, String.class);
        JSONObject jsonRes = JSON.parseObject(res);
        /**
         * {"msg":"ok","result":"0x0","code":0,"extra":"983b3b5cb95d7efc9190be44c824c51e"}
         */
        log.info("获取地址：{} nonce 响应:{}", fromAddress, jsonRes);
        String code = jsonRes.getString("code");
        if (!"0".equals(code) || "".equals(jsonRes.getString("nonce"))) {
            throw new CustomException("获取nonce失败");
        }

        BigInteger nonce = new BigInteger(jsonRes.getString("result").replace("0x", ""), 16);
        String symbol = jsonRes.getString("extra");

        BigInteger amountWei = toWei(amount.toString());
        //转为16进制

        String dataSend = genErc20Transfer(toAddress, amountWei);

        BigInteger gasLimit = getGasLimit(fromAddress, COIN_ADDRESS, "0x0", dataSend);

        BigInteger gasPrice = Convert.toWei("2", Convert.Unit.GWEI).toBigInteger();

        RawTransaction transaction = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, COIN_ADDRESS, BigInteger.ZERO, dataSend
        );

        // 用私钥签名交易
        Credentials credentials = Credentials.create(privateKey);
        byte[] signedMessage = TransactionEncoder.signMessage(transaction, 708, credentials);
        String rawTx = Numeric.toHexString(signedMessage);

        // 准备POST请求数据
        MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();
        postData.add("raw", rawTx);
        postData.add("symbol", symbol);
        postData.add("version", "999999");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(postData, headers);


        String response = restTemplate.postForEntity(RAW_TX_URL, request, String.class).getBody();
        // 输出响应
        /**
         * {"code":0,"msg":"ok","result":{"result":"0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501","error":null},"error":null}
         */

        System.out.println(response);
        JSONObject jsonResponse = JSON.parseObject(response);
        String codeResponse = jsonResponse.getString("code");
        if (!"0".equals(codeResponse) || "".equals(jsonResponse.getString("result"))) {
            throw new CustomException("提现失败");
        }

        JSONObject result = jsonResponse.getJSONObject("result");
        return result.getString("result");
    }


    private static String encodeFunctionCall(ContractFunction function, String fromAddress, String toAddress, BigInteger amount) {
        String functionSignature = function.getName() + "(" + function.getInputs() + ")";
        String encodedFunction = functionSignature + fromAddress + toAddress + amount.toString(16);
        // 确保 `encodedFunction` 使用适当的格式进行编码


        return encodedFunction;
    }


    /**
     * @param from  发送地址
     * @param to    接收地址
     * @param value 金额
     * @param data  数据
     * @return
     */
    public BigInteger getGasLimit(String from, String to, String value, String data) {
        try {
            String url = GAS_LIMIT_URL + "?from=" + from + "&to=" + to + "&value=" + value + "&data=" + data;
            String response = restTemplate.getForObject(url, String.class);
            log.info("获取gas 响应:{}", response);
            /**
             * {"code":0,"msg":"ok","result":"0x5208","error":null}
             */
            if (response == null || "".equals(response)) {
                throw new CustomException("获取GasLimit失败");
            }
            JSONObject jsonRes = JSON.parseObject(response);
            String code = jsonRes.getString("code");
            if (!"0".equals(code) || "".equals(jsonRes.getString("result"))) {
                throw new CustomException("获取GasLimit失败");
            }

            String gasLimitStr = jsonRes.getString("result");
            return new BigInteger(gasLimitStr.replace("0x", ""), 16);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常
            return BigInteger.ZERO; // 返回 0
        }
    }

    // 获取合约余额
    public BigDecimal getBalanceOfContract(String address) {
        try {
            // 构建数据
            String data = "0x70a08231" + Numeric.toHexStringNoPrefixZeroPadded(Numeric.toBigInt(address), 64);
            String url = OLINK_CALL_URL + "?to=" + COIN_ADDRESS + "&data=" + data;
            String response = restTemplate.getForObject(url, String.class);
            JSONObject responseJson = JSON.parseObject(response);
            String code = responseJson.getString("code");
            log.info("获取合约：{} 余额 响应:{}", address, response);
            if (!"0".equals(code) || "".equals(responseJson.getString("result"))) {
                return BigDecimal.ZERO;
            }

            String balanceStr = responseJson.getString("result");

            BigInteger oldBalance = hexToBigInt(balanceStr);

//            double balance = oldBalance.longValue() / Math.pow(10, 18);
//            return BigDecimal.valueOf(balance);

            //BigInteger / 10的18次方
            return new BigDecimal(oldBalance).divide(new BigDecimal("1000000000000000000"));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常
            return BigDecimal.ZERO;
        }
    }

    // 获取地址的余额
    public BigDecimal getBalance(String address) {
        try {
            String url = "/api/balance?addr=" + address;
            String response = restTemplate.getForObject(url, String.class);
            JSONObject responseJson = JSON.parseObject(response);
            String code = responseJson.getString("code");
            log.info("获取地址：{} 余额 响应:{}", address, response);
            if (!"0".equals(code) || "".equals(responseJson.getString("result"))) {
                return BigDecimal.ZERO;
            }
            String balanceStr = responseJson.getString("result");
            long oldBalance = Long.parseLong(balanceStr.replace("0x", ""), 16);
            double balance = oldBalance / Math.pow(10, 18);

            return BigDecimal.valueOf(balance);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常
            return BigDecimal.ZERO; // 返回 0
        }
    }

    /**
     * 生成 ERC20 代币转账数据
     *
     * @param to     接收地址
     * @param amount 金额
     * @return
     */
    public String genErc20Transfer(String to, BigInteger amount) {
        // 将 BigInteger 转换为十六进制字符串
        String amountHex = amount.toString(16);

        // 计算需要补齐的 0 的数量
        int padLength = 64 - amountHex.length();

        // 使用 StringBuilder 来构建补齐后的十六进制字符串
        StringBuilder paddedAmountHex = new StringBuilder();
        for (int i = 0; i < padLength; i++) {
            paddedAmountHex.append('0');
        }
        paddedAmountHex.append(amountHex);

        // 添加前缀并拼接最终字符串
//        return "0xa9059cbb" + addPad(to) + paddedAmountHex.toString();
        return addPad("0xa9059cbb", to) + paddedAmountHex.toString();
    }

    // 补齐地址
    private String addPad(String address) {
        return "0x" + "000000000000000000000000" + address.substring(2);
    }

    private String addPad(String prefix, String address) {
        return prefix + "000000000000000000000000" + address.substring(2);
    }

    // 计算费用
    public double calcFee(int gasPrice, int gasLimit, boolean isE9) {
        BigInteger totalCost = BigInteger.valueOf(gasPrice).multiply(BigInteger.valueOf(gasLimit));
        if (isE9) {
            return totalCost.doubleValue() / Math.pow(10, 9);
        } else {
            return totalCost.doubleValue() / Math.pow(10, 18);
        }
    }

    public static BigInteger toWei(String amount) {
//        if (amount == null || amount.isEmpty()) {
//            return BigInteger.ZERO;
//        }
//
//        // 检查小数点的位置
//        int decimalIndex = amount.indexOf('.');
//
//        BigInteger unitFactor = BigInteger.TEN.pow(18);
//
//        // 如果没有小数点，直接转换为 BigInteger
//        if (decimalIndex == -1) {
//            return new BigInteger(amount).multiply(unitFactor);
//        }
//
//        // 移除小数点
//        String amountWithoutDot = amount.replace(".", "");
//
//        // 计算小数点后的位数
//        int decimalPlaces = amount.length() - decimalIndex - 1;
//
//        // 转换为 wei
//        BigInteger amountInWei = new BigInteger(amountWithoutDot).multiply(unitFactor)
//                .divide(BigInteger.TEN.pow(decimalPlaces));
//        return amountInWei;
        return Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
    }

    public TransactionRecords getTx(String txHash) {
        /**
         * {
         *   "code": 0,
         *   "msg": "ok",
         *   "result": {
         *     "hash": "0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501",
         *     "blockNumber": "0x484ff9",
         *     "blockHash": "0x2a569be8d1cddce3ea80422d3f8fe555af2a0e0791e900e2ad96e3ef51e60aab",
         *     "from": "0xf0413fe3410657ab377e73ebf0028c45fab29b7b",
         *     "to": "0x1e27bdf3a7b074f4d026b371b788f1625c95ee9c",
         *     "value": "0x1bc16d674ec80000",
         *     "gas": "0x5208",
         *     "gasPrice": "0x77359400",
         *     "time": "0x66b18a83",
         *     "input": "0x"
         *   }
         * }
         */

        try {
            String res = restTemplate.getForObject(GET_TX_INFO + txHash, String.class);
            JSONObject jsonRes = JSON.parseObject(res);
            log.info("获取交易信息：{} 响应:{}", txHash, jsonRes);
            String code = jsonRes.getString("code");
            if (!"0".equals(code) || "".equals(jsonRes.getString("result"))) {
                throw new CustomException("获取交易信息失败");
            }
            if (jsonRes == null || "".equals(jsonRes.getString("result"))) {
                throw new CustomException("获取交易信息失败");
            }
            String resultStr = jsonRes.getString("result");
            return JSON.parseObject(resultStr, TransactionRecords.class);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static BigInteger hexToBigInt(String hex) {
        if (hex != null && !hex.isEmpty()) {
            return new BigInteger(hex.substring(2), 16);
        } else {
            return BigInteger.ZERO;
        }
    }

    /**
     * @return 交易哈希
     */
    public String recharge(String fromAddress, String privKey, BigDecimal amount) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("金额必须大于0");
            }
            String toAddress = redPacketConfigService.getConfig(RedPacketConfigKeyEnums.CENTER_SERVER_ADDRESS.getKey());

            //使用restTemplate调用接口
            String res = restTemplate.getForObject(INFURA_URL + "?addr=" + fromAddress, String.class);
            JSONObject jsonRes = JSON.parseObject(res);
            /**
             * {"msg":"ok","result":"0x0","code":0,"extra":"983b3b5cb95d7efc9190be44c824c51e"}
             */
            log.info("获取地址：{} nonce 响应:{}", fromAddress, jsonRes);
            String code = jsonRes.getString("code");
            if (!"0".equals(code) || "".equals(jsonRes.getString("nonce"))) {
                throw new CustomException("获取nonce失败");
            }

            BigInteger nonce = new BigInteger(jsonRes.getString("result").replace("0x", ""), 16);
            String symbol = jsonRes.getString("extra");

            BigInteger amountWei = toWei(amount.toString());
            //转为16进制
            String amountHax = Numeric.toHexString(amountWei.toByteArray());

            BigInteger gasLimit = getGasLimit(fromAddress, toAddress, amountHax, "0x");

            BigInteger gasPrice = Convert.toWei("2", Convert.Unit.GWEI).toBigInteger();

            // 创建原始交易
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, gasPrice, gasLimit, toAddress, amountWei);

            // 用私钥签名交易
            Credentials credentials = Credentials.create(privKey);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, 708, credentials);
            String rawTx = Numeric.toHexString(signedMessage);


            // 准备POST请求数据
            MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();
            postData.add("raw", rawTx);
            postData.add("symbol", symbol);
            postData.add("version", "999999");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(postData, headers);

            String response = restTemplate.postForEntity(RAW_TX_URL, request, String.class).getBody();
            // 输出响应
            /**
             * {"code":0,"msg":"ok","result":{"result":"0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501","error":null},"error":null}
             */

            JSONObject jsonResponse = JSON.parseObject(response);
            String codeResponse = jsonResponse.getString("code");
            if (!"0".equals(codeResponse) || "".equals(jsonResponse.getString("result"))) {
                throw new CustomException("充值失败");
            }

            JSONObject result = jsonResponse.getJSONObject("result");
            return result.getString("result");
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常
            return null; // 返回 null
        }
    }

    public List<TransactionRecords> getOwlTxs(String address) {
        try {

            //查询配置表中的page_size
            String pageSize = redPacketConfigService.getConfig("page_size");

            // 发送GET请求
//            String res = restTemplate.getForEntity("/api/getErc20Txs?addr=" + addPad(address) + "&token=" + COIN_ADDRESS, String.class).getBody();
            String res = restTemplate.getForEntity("/api/getAddrTokenTxs?addr=" + addPad(address) + "&token=" + COIN_ADDRESS
                            + "&pageIndex=1&pageCount=" + pageSize
                    , String.class).getBody();
            /**
             * {
             * 	"code": 0,
             * 	"msg": "ok",
             * 	"total": 0,
             * 	"list": [
             *                {
             * 			"from": "0x0000000000000000000000002211df9f5681350059481af0bd226639cb37297b",
             * 			"to": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"value": "0x000000000000000000000000000000000000000000000000016345785d8a0000",
             * 			"time": "0x66bef701",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x0a4780a7744a343e0fb3f5ac451faa6840ac055f712c058c3ae1e6f821f64b7a",
             * 			"blockNumber": "0x495f63",
             * 			"blockHash": "0x3f9f5d93085288f2a7c31fc87ad12db89c0ab4af742add270d3fa01f49d5370a"
             *        },
             *        {
             * 			"from": "0x000000000000000000000000e77874b9f72b2e568217e5af234dc0cebc62383f",
             * 			"to": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"value": "0x00000000000000000000000000000000000000000000000098a7d9b8314c0000",
             * 			"time": "0x66bef5d9",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x910bf9eb9029b9362bd2aa29aa1d549e63eb116a435ce4fd73be3d7979c92825",
             * 			"blockNumber": "0x495f50",
             * 			"blockHash": "0x5b23f49cb0aae1d98099448af76799e92ed8a87b8efdc30f972b1ed839bafd64"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"to": "0x000000000000000000000000971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3",
             * 			"value": "0x00000000000000000000000000000000000000000000000029a2241af62c0000",
             * 			"time": "0x66beee58",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x35089844006c411e282e7088400dcdd1e8c7c2424806171609ccb6bfb803d346",
             * 			"blockNumber": "0x495eb4",
             * 			"blockHash": "0xc5f380a55bb1c551a4cb590943bc41f9b56708b34a4641a019b055cfe37bc2f6"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"to": "0x000000000000000000000000173dc3b48f59aa2876e89b2c4955d4133832eaec",
             * 			"value": "0x000000000000000000000000000000000000000000000000016345785d8a0000",
             * 			"time": "0x66beec6a",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0xe8ad1c4a669c2889eeee3bacc9585b0f83f840d199fa76cca2529ee6385d103d",
             * 			"blockNumber": "0x495e8b",
             * 			"blockHash": "0xc124d5a12525d9f91d17e0926461b692ae03506776127efcc59f5bbd91d52d59"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"to": "0x0000000000000000000000002211df9f5681350059481af0bd226639cb37297b",
             * 			"value": "0x000000000000000000000000000000000000000000000000016345785d8a0000",
             * 			"time": "0x66beb704",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x114a32fcf4b95b0164c8712ea80d12631fdda1f379d32968e27a5ea173dcc1ab",
             * 			"blockNumber": "0x495a67",
             * 			"blockHash": "0xdf0f6b852c4d213f3baa9067529a45aacd8028b3ea55639229a1636b220871af"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"to": "0x000000000000000000000000f0413fe3410657ab377e73ebf0028c45fab29b7b",
             * 			"value": "0x00000000000000000000000000000000000000000000000002c68af0bb140000",
             * 			"time": "0x66be007f",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x34fca132250cfc61388af9c5dffa400e890dc020eb60fb5e59f878746e8577d8",
             * 			"blockNumber": "0x494c23",
             * 			"blockHash": "0xa7a3eeb1d93ff946bedcf44a2ae3ff2e1e3d1febecadc5fda324b998db92c948"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"to": "0x000000000000000000000000f0413fe3410657ab377e73ebf0028c45fab29b7b",
             * 			"value": "0x00000000000000000000000000000000000000000000000002c68af0bb140000",
             * 			"time": "0x66bdfaa8",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x51ececfa35e5ffb566476a401a8e5a97208a68505c1a3c76ef928baeb90be7dc",
             * 			"blockNumber": "0x494bac",
             * 			"blockHash": "0xf88509e11d87558abd9d2f4edb8b11ed2b7cdbf5e4b752b28ebaeb2c48800377"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"to": "0x000000000000000000000000f0413fe3410657ab377e73ebf0028c45fab29b7b",
             * 			"value": "0x00000000000000000000000000000000000000000000000002c68af0bb140000",
             * 			"time": "0x66bdfa8a",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x47deaed9e23ff8162b1272aa693331c74212ad9f45f873a17fdb87d1f22b8cfa",
             * 			"blockNumber": "0x494bab",
             * 			"blockHash": "0x79d13b5ea9f58ea13d1ce64898438aed1b8fbf0c42d46550baa1eb1f5d58a307"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"to": "0x000000000000000000000000f0413fe3410657ab377e73ebf0028c45fab29b7b",
             * 			"value": "0x00000000000000000000000000000000000000000000000002c68af0bb140000",
             * 			"time": "0x66bdf55f",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x7ce32d793671f97f9cd148b43915345c5c82b527612184274b606f7eb1278944",
             * 			"blockNumber": "0x494b49",
             * 			"blockHash": "0xbbe77905e6ef04e3c45e588de3e545ca2d4dadeed4406f7d4ebae1003736ba6d"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000002211df9f5681350059481af0bd226639cb37297b",
             * 			"to": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"value": "0x0000000000000000000000000000000000000000000000008ac7230489e80000",
             * 			"time": "0x66bdf2d4",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0xef82b3f5620ee9a2e3eb2c84a0756f81f455f5e9114d40946fbd2b43a7c3a6d2",
             * 			"blockNumber": "0x494b0e",
             * 			"blockHash": "0xaaf3d76dd3eb7dc476403e99796e9168192b4b779766d2cdba47d36b14310abf"
             *        },
             *        {
             * 			"from": "0x0000000000000000000000002211df9f5681350059481af0bd226639cb37297b",
             * 			"to": "0x0000000000000000000000009fedcfd583d563aab3b067315cfdc55354317f82",
             * 			"value": "0x0000000000000000000000000000000000000000000000003782dace9d900000",
             * 			"time": "0x66892651",
             * 			"token": "0x1c4c015d144e7e4fbcb51394d03c635549be9cd5",
             * 			"hash": "0x53e48c27588a22a0dcd740e93f6b59f8339a0f4cce1ae04eb325a330ca796e7a",
             * 			"blockNumber": "0x451ed9",
             * 			"blockHash": "0xa34732cbe13c255cf785456ffe665defdabf7cf47042bfa52cb1332f3b11822b"
             *        }
             * 	]
             * }
             */

            JSONObject jsonRes = JSON.parseObject(res);
            String code = jsonRes.getString("code");
            if (!"0".equals(code) || "".equals(jsonRes.getString("list"))) {
                throw new CustomException("获取交易列表失败");
            }

            JSONArray list = jsonRes.getJSONArray("list");
            List<TransactionRecords> txs = list.toJavaList(TransactionRecords.class);

            return txs;

        } catch (Exception e) {
            System.err.println("Web3Util-getOwlTxs error = " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public String withdraw(String toAddress, Double amount) {
        String fromAddress = redPacketConfigService.getConfig(RedPacketConfigKeyEnums.CENTER_SERVER_ADDRESS.getKey());
        String privateKey = redPacketConfigService.getConfig(RedPacketConfigKeyEnums.CENTER_SERVER_PRIVATE_KEY.getKey());

        //使用restTemplate调用接口
        String res = restTemplate.getForObject(INFURA_URL + "?addr=" + fromAddress, String.class);
        JSONObject jsonRes = JSON.parseObject(res);
        /**
         * {"msg":"ok","result":"0x0","code":0,"extra":"983b3b5cb95d7efc9190be44c824c51e"}
         */
        log.info("获取地址：{} nonce 响应:{}", fromAddress, jsonRes);
        String code = jsonRes.getString("code");
        if (!"0".equals(code) || "".equals(jsonRes.getString("nonce"))) {
            throw new CustomException("获取nonce失败");
        }

        BigInteger nonce = new BigInteger(jsonRes.getString("result").replace("0x", ""), 16);
        String symbol = jsonRes.getString("extra");

        BigInteger amountWei = toWei(amount.toString());
        //转为16进制
        String amountHax = Numeric.toHexString(amountWei.toByteArray());

        BigInteger gasLimit = getGasLimit(fromAddress, toAddress, amountHax, "0x");

        BigInteger gasPrice = Convert.toWei("2", Convert.Unit.GWEI).toBigInteger();


        // 创建原始交易
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce, gasPrice, gasLimit, toAddress, amountWei);

        // 用私钥签名交易
        Credentials credentials = Credentials.create(privateKey);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, 708, credentials);
        String rawTx = Numeric.toHexString(signedMessage);

        // 准备POST请求数据
        MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();
        postData.add("raw", rawTx);
        postData.add("symbol", symbol);
        postData.add("version", "999999");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(postData, headers);

        String response = restTemplate.postForEntity(RAW_TX_URL, request, String.class).getBody();
        // 输出响应
        /**
         * {"code":0,"msg":"ok","result":{"result":"0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501","error":null},"error":null}
         */

        JSONObject jsonResponse = JSON.parseObject(response);
        String codeResponse = jsonResponse.getString("code");
        if (!"0".equals(codeResponse) || "".equals(jsonResponse.getString("result"))) {
            throw new CustomException("提现失败");
        }

        JSONObject result = jsonResponse.getJSONObject("result");
        return result.getString("result");
    }

    public Double getRealAmount(String amount) {
        if (amount == null || "".equals(amount)) {
            return 0.0;
        }
        //amount:0x1bc16d674ec80000
        String amountStr = amount.replace("0x", "");
        BigInteger amountBigInt = new BigInteger(amountStr, 16);
        return amountBigInt.doubleValue() / Math.pow(10, 18);
    }

    public Double getOwlRealAmount(String amount) {
        //amount:0x000000000000000000000000000000000000000000000000016345785d8a0000
        if (amount == null || "".equals(amount)) {
            return 0.0;
        }
        //amount:0x1bc16d674ec80000
        String amountStr = amount.replace("0x", "");
        BigInteger amountBigInt = new BigInteger(amountStr, 16);
        return amountBigInt.doubleValue() / Math.pow(10, 18);
    }

    //获取16进制的余额
    public String getBalanceHex(Double balance) {
        try {
            BigInteger balanceWei = toWei(balance.toString());
            return Numeric.toHexString(balanceWei.toByteArray());
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常
            return "0x0"; // 返回 0
        }
    }

    public String getAbi() {
        //获取类路径下的abi.json文件
        String abi = null;

        try {
            InputStream resourceAsStream = TransactionController.class.getClassLoader().getResourceAsStream("abi.json");
            byte[] bytes = new byte[resourceAsStream.available()];
            resourceAsStream.read(bytes);
            abi = new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("获取abi失败");
        }

        return abi;

    }
}
