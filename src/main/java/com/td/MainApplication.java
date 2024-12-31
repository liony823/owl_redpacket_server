package com.td;

import com.td.client.service.ContributionValueService;
import com.td.common.utils.RedisUtils;
import com.td.common.utils.TokenUtils;
import com.td.common.utils.Web3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Td
 * @email td52512@qq.com
 * @date 2024-04-01 15:55
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableMongoRepositories
public class MainApplication implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Web3Util web3Util;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ContributionValueService contributionValueService;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
        log.info("服务器启动成功！");
    }

    @Override
    public void run(String... args) throws Exception {
//        PrivateRedPacketExpireServiceImpl privateRedPacketExpireServiceImpl = (PrivateRedPacketExpireServiceImpl) beanFactory.getBean("private_RedPacketExpireServiceImpl");
//        String tokenByUserId = privateRedPacketExpireServiceImpl.getTokenByUserId("9067943559");
//        privateRedPacketExpireServiceImpl.sendRedPacketRefundMsg("9067943559","4872251310",new RedPacket());

//        BigDecimal balance1 = web3Util.getBalance("0xf0413fe3410657ab377e73ebf0028c45fab29b7b");
//        BigDecimal balance2 = web3Util.getBalance("0x9fedcfd583d563aab3b067315cfdc55354317f82");
//        log.error("balance1:{}", balance1);
//        log.error("balance2:{}", balance2); BigDecimal balance1 = web3Util.getBalance("0xf0413fe3410657ab377e73ebf0028c45fab29b7b");
//        BigDecimal balance1 = web3Util.getBalanceOfContract("0xf0413fe3410657ab377e73ebf0028c45fab29b7b");
//        BigDecimal balance2 = web3Util.getBalanceOfContract("0x9fedcfd583d563aab3b067315cfdc55354317f82");
//        BigDecimal balance3 = web3Util.getBalanceOfContract("0x971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3");
//        BigDecimal balance4 = web3Util.getBalance("0x971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3");
//        log.error("balance1:{}", balance1);
//        log.error("balance2:{}", balance2);
//        log.error("balance3:{}", balance3);
//        log.error("balance4:{}", balance4);


//        web3Util.getTx("0x4dde5b01f30997ea7fcbf7e5bbe53bcf12eae9cafa383ebaba870b6c3f021501");
//        log.error("balance:{}", balance);

//        String hash = web3Util.transfer("0xf0413fe3410657ab377e73ebf0028c45fab29b7b", "0x9fedcfd583d563aab3b067315cfdc55354317f82", "35da9ac643c66739a9ac2e35c799f63080b880ce28836934e32d4168c745fe67", new BigDecimal("3"));

        //0xd6596a36ee248d624602d70b3926b7681dd881db6c00f21627d7045a79352969

        //0xeaccd99828d6321a6008c2a71dbf23361e740ead76d8d60f1e82dd8bb6cee2cd

//        String hash = web3Util.recharge("0x9fedcfd583d563aab3b067315cfdc55354317f82", "95099a6e7d13037d957c20243a398ca44a4a5e19bfed214e690776b9768c2749", new BigDecimal("2"));
//        System.out.println(hash);//0xc36ef6e0d0c1b2228c3bfaf97764620b67e54ef339d46decd4f5dd4b644bdb06

//        String hash = web3Util.withdraw("0x9fedcfd583d563aab3b067315cfdc55354317f82", new BigInteger("3"));

        //0x68636fc8f2ef346b6a4205bde54b53eb8521e5c90b09e2392296504aecc5a164
        //0x24522df6a3864ab86e97bb08911004f9d1c2aadf46d3dc77887bda541d1e0395
//        TransactionRecords tx = web3Util.getTx(hash);
//        System.out.println(tx);

        //查询充值结果
//        web3Util.getTx(hash);

        //0xeaccd99828d6321a6008c2a71dbf23361e740ead76d8d60f1e82dd8bb6cee2cd
        //0x7baacd2c5bfefde5ac9b45aec26bc5ff0c7008bde68904d3c72cc47d31e2c7bc


        //查询所有user表中的数据
//        List<User> users = mongoTemplate.findAll(User.class);
//        for (User user : users) {
//            log.info("user:{}", user);
//        }
        //查询user_id为1的用户
//        Query query = new Query(Criteria.where("userId").is("1"));
//        System.out.println(mongoTemplate.findOne(query, RedPacketBalance.class));

//        HashMap<String, Object> map = new HashMap<>();
////        map.put("userId", "2719400749"); //eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOiIyNzE5NDAwNzQ5In0.q5dmK_ppolaZn0f6o__h1FujZSaeME2QcFYKcSUJ1OO7TkC9jfxPHAXtvQwX9iz1
////        map.put("userId", "8483075095"); //eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOiI4NDgzMDc1MDk1In0.CSuM4D1Dh8Yl3obDpdEyXkpkLJC_3H8lV0O7CGwMyDA48WqrN_5dCnxrL42uiKJR
//        map.put("userId", "imAdmin"); //eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOiI4NDgzMDc1MDk1In0.CSuM4D1Dh8Yl3obDpdEyXkpkLJC_3H8lV0O7CGwMyDA48WqrN_5dCnxrL42uiKJR
//
//        String token = tokenUtils.createToken(map);
//        System.out.println(token);

//        HashMap<String, Object> map = new HashMap<>();
//        map.put("UserID", "9067943559");
//        map.put("UserID", "4080787166");
//        String token = tokenUtils.createToken(map);
//        System.out.println(token);//eyJhbGciOiJIUzI1NiJ9.eyJVc2VySUQiOiI0MDgwNzg3MTY2IiwiaWF0IjoxNzIzODY0MjA5fQ.ukM9h-Ct0M3j4PUCCx6OylNKYnGD5_UDfztIONRMZAQ 4080787166
        //eyJhbGciOiJIUzI1NiJ9.eyJVc2VySUQiOiI5MDY3OTQzNTU5IiwiaWF0IjoxNzIzNzEwMjcxfQ._6X56f2g8ZzF8WoM7E8BbFzuPP-JgVYt4jlTOigypHk 9067943559

//        System.out.println(tokenUtils.parseToken(token));

//        String s = web3Util.transferOWL("0x9fedcfd583d563aab3b067315cfdc55354317f82", "0xf0413fe3410657ab377e73ebf0028c45fab29b7b", "95099a6e7d13037d957c20243a398ca44a4a5e19bfed214e690776b9768c2749", new BigDecimal("12"));
//        String s = web3Util.transferOWL("oc971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3", "0x9fedcfd583d563aab3b067315cfdc55354317f82", "oc971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3", new BigDecimal("3"));
//        String s = web3Util.transferOWL("0x9fedcfd583d563aab3b067315cfdc55354317f82", "0x971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3", "95099a6e7d13037d957c20243a398ca44a4a5e19bfed214e690776b9768c2749", new BigDecimal("3"));
//        System.out.println(s);//0x8a67448f6228d2d3e84fec79014be4ddb4bcbf12e9f0e59a8d6bcb9b946b88f5 0x22ab07b7e67326fc06d3053640ed2ea5d6b9053a7f799a361fa14041b7f64ff2 0xc533ab80a0ca31bb46d45f81d65493b77fef3954c827e7f33b25289b587751ea

//        TransactionRecords tx = web3Util.getTx("0xdbc6ba5230033c15cdb3c80fb0bb6625769467175efa3f88a229b2d9fbbb0b5d");
//        System.out.println(tx);

//        String owlTxs = web3Util.getOwlTxs("0x9fedcfd583d563aab3b067315cfdc55354317f82");
//        System.out.println(owlTxs);

//        String s = web3Util.transferOWL("0xf0413fe3410657ab377e73ebf0028c45fab29b7b", "0x971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3", "35da9ac643c66739a9ac2e35c799f63080b880ce28836934e32d4168c745fe67", new BigDecimal("3"));
//        String s = web3Util.transferOWL("0x971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3", "0xf0413fe3410657ab377e73ebf0028c45fab29b7b", "c193f5f7c842b2c238ee2bc91d3d8ac70be82b94f7f9393a6709cb26b5fcfe9e", new BigDecimal("3"));
//        System.out.println(s);
        //提现 0x926db726d86f112f370b2ff8f333b488bcdfc514f0601812d429ffb5a644a24b
        //充值 0xe94f87a00a06e3365627a7285c79aeaf78c14e5ea033f3b59aeeb9262c999a06

//        balance1 = web3Util.getBalanceOfContract("0xf0413fe3410657ab377e73ebf0028c45fab29b7b");
//        balance2 = web3Util.getBalanceOfContract("0x9fedcfd583d563aab3b067315cfdc55354317f82");
//        balance3 = web3Util.getBalanceOfContract("0x971eabc8ac03f6b0ce9f881f7e2bb6944e0263f3");
//        log.error("balance1:{}", balance1);
//        log.error("balance2:{}", balance2);
//        log.error("balance3:{}", balance3);
    }


    //token：eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOiIyNzE5NDAwNzQ5In0.q5dmK_ppolaZn0f6o__h1FujZSaeME2QcFYKcSUJ1OO7TkC9jfxPHAXtvQwX9iz1
//    public String createToken(Map<String, Object> claims) {
//        JWTSigner jwtSigner = JWTSignerUtil.hs384(Base64.encode("525150").getBytes(StandardCharsets.UTF_8));
//        return JWTUtil.createToken(claims, jwtSigner);
//    }
}
