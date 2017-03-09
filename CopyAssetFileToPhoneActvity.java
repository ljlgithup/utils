package com.example.mobile.safe.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.example.mobile.safe.R;
import com.example.mobile.safe.R.layout;
import com.example.mobile.safe.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class NumberQueryActvity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_query_actvity);
		//把asset资产目录里面的数据库文件(在apk里面的)拷贝到手机系统里面
		try {
			InputStream in = getAssets().open("address.db");
			File file = new File(getFilesDir(),"address.db");
			FileOutputStream out = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int len = -1;
			while((len = in.read(bytes))!=-1){
				out.write(bytes, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
