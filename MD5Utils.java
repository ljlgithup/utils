import java.security.MessageDigest;
/**
* MD5���ܷ�ʽ������ת ԭ��-->����
* ���Ĳ�����ת����ԭ��   
* ʹ�ó��� ��������ܺ�洢��sharePreference�С���������������ʱ�������������ܺ�	
* ���бȽϸ�֮ǰ����������ж��Ƿ�һ�¡�
*/
public class MD5Utils {

	public static void EnCoder(String string) throws Exception{
		MessageDigest digest = MessageDigest.getInstance("md5");
		String password = string;
		byte[] result = digest.digest(password.getBytes());
		StringBuilder sb = new StringBuilder();
		for(byte b : result){
			int number = b&0xff-3;//����
			String str = Integer.toHexString(number);
			if(str.length()==1){
				sb.append("0");
			}
			sb.append(str);
		}
		System.out.println(sb.toString());
	}

}
