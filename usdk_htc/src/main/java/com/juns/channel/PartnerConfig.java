package com.juns.channel;

/**
 * CP配置更改项 
 * @author 
 *
 */
public class PartnerConfig {
	
	//合作网游CP的RSA PKCS8转码私钥
	//注意：这里需要填写的是OpenSSL工具生成转码后的私钥（private_key_pkcs8.txt文件内容），不是私钥（private_key.pem）
	public static final String CP_PRIVATE_KEY_PKCS8 ="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMSXps9RJa5aPRYd" +
			"YS/bOdtjSzHMxsRFIoIH+EqTp+Rioq3rN8gimlyGgv1IXVY2hW+oZx+1kdE7Tc3t" +
			"opch7EwzCTasgCZS4vOk01Xsbz4YAU4Fdx5KEPybpcXDSHjGkFg5tTgNaS7f3ZUX" +
			"VX4FMcu3JWfguoR5mIxz9TsauD0xAgMBAAECgYEAvTyxoIyfNfa9KIV9YOCpD8rD" +
			"vhs9Ff7qmbpsQfViPpDHwZxTJL5SUlBU1NSMrAltZq73HWndBEgOu/RMdyh7WKn6" +
			"bthQbHve3FOGNnhwQplRpSZl7ladq6z9sR3VSBqI6NiHGJypH93IFqMElEj+OY6x" +
			"H+3CW0g6CTrqvvrmP+UCQQDoWNzX0uXkXs49tsxyVDH3B+1MOOoeaVgyw102l3OE" +
			"Nb3GJoo//S7Q/LApAy5WiKgMK/d73BOdtuPj1QenBzpTAkEA2Jr+iNbmYhO0rXij" +
			"RnPGmK0w0pXApQnIwCifszAy9Q+RrRq0S9aaKWTNbjeYmj4ktxW0ZQnGuFos9s9Y" +
			"bQsh6wJAbOhSQ/u/9GaX54eKl0Bt1yvpB+RZoP2h8NFwHMmqxNrn5Dxmaw4C5sS6" +
			"0c6C5t7RXjsxmuYSb8etVDqjhqA1rwJALS1eq2HPGWOsyoWf4GDjahKtXS64yHNa" +
			"+KFAddo2tqAxrR4lvq+a5clNg5B69qjBdaJERtKcLPU57ZxfgBbTUwJBAK8FGwW6" +
			"v5SAiAEnfId00p+KI2iGxME3Qvcsc94LSPwaPXl0hgYLLE/AfnmFPAqNE1Op9Myp" +
			"H2kme9c7fkW+fYE=";
	
	//合作网游CP的RSA公钥
	public static final String CP_PUBLIC_KEY="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCjt1WsiB7tqIPt0aid7SU96dYMDr8cMJHkLdDZMsb0+J64XHQRTPjbVE5hg32KNI8mJDvTGuo4XaqB/EDM5VFMFKA/AI62wB1cTMVar0CD3CGeS/AsEjvMm+hlCLetlqptiPK7yY8o/ziXQN0Q9P7AIlMQ4cXQTutaiQHrsRZmwIDAQAB";
	
	//HTC游戏中心RSA公钥(用于帐号合法验证和支付成功合法验证)
	//注意：请替换成对接群里最新的HTC公钥，此公钥为DEMO使用公钥
	public static final String JOLO_PUBLIC_KEY="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDbRLzWfCD4pQb1mjeGLy6gw+AfOKZ1dpNbMUyZml+p3stTSdTyHHpkuPPsaOqsT9gFDSmXz5KRBt4w6KCeLj/R61KA5rmMJipDnSJV19kld0z6NW47kiEQHslaalDBCST94TUIcCzjhaiG3yTChDCTFo3v47qyt6j3YvVpih8UNQIDAQAB";
	
	//CP支付接收通知URL(支付回调地址)
	public static final String CP_NOTIFY_URL = "http://mpay.junshanggame.com/pay/ptpay/68/100050/";
	
	//CP从HTC游戏中心商务获取到的游戏合作唯一id  gamecode=test8888
	public static final String CP_GAME_CODE = "3242435056968";

	//CP游戏名称
	public static final String CP_GAME_NAME = "猛龙过江";
	
}

