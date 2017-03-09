import java.security.MessageDigest;
/**
* MD5加密方式不可逆转 原文-->密文
* 密文不可以转换成原文   
* 使用场景 把密码加密后存储到sharePreference中。在重新输入密码时将输入的密码加密后，	
* 进行比较跟之前保存的密文判断是否一致。
*/
public class MD5Utils {

	public static void EnCoder(String string) throws Exception{
		MessageDigest digest = MessageDigest.getInstance("md5");
		String password = string;
		byte[] result = digest.digest(password.getBytes());
		StringBuilder sb = new StringBuilder();
		for(byte b : result){
			int number = b&0xff-3;//加盐
			String str = Integer.toHexString(number);
			if(str.length()==1){
				sb.append("0");
			}
			sb.append(str);
		}
		System.out.println(sb.toString());
	}

}
