package com.juns.encrypt;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.juns.sdk.framework.safe.JunSEncrypt;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.textView);
        String content = "lJYRlz9%2Fnw4FP%2FpzzwX47qEtpcYR6cHEDqOc2NYgL1jCo790mAVdyvpKW0g%2Bl9JVnxuZM3jUBihS%0A1RlRsux9Z%2FeE85Id0i0a%2BLY9xXHfJ6u48Nk5uxgwDrpDkeEcySEKZcSwgpe9o46KNi6bWUTQdu5U%0APHc%2Fm8jjtSdsrOgUEr1zgHLzcUrXjb%2BgUzjrtMeOdn09FGFAE2bXqqJmt4nxxNQhIGMsv%2B3dpUfp%0AA3JqlM%2B3XuhzQv%2BSB4TVYuP3JZQJM9T8QrbdJW3w9DXZGBfyJAPScGAPC6iXFflVNVp988Z79ab%2B%0AE2Lj4LsM%2BGFm2lmlEWJTB9CSW%2BJaIFqFTV7v2CNKw0T7qs1JpiEYyFqBQmoIw2jqmLDjCpLVqfLn%0A4nrtif5tMFtfWKlWa%2BQoDUkDdicCdV3WFYMaZxXZS%2Bz8mofTdcMTq32S8dEd%2BIj49AaD31SRfrfq%0ABffvjP3308unDTKW1CImVKq44Br6eVIOPaNN0OV%2F7GVYRYmRNMVAxfR3GDoNF3wEmzBVlP8Z5eLZ%0AT9J7ft0koTkKiwxnlbQQtjRDiBT56DzwDlF0JWAW9KWQruf4YLvGfdr3z9PkQl7MECpOBxrCDHqE%0Ao%2FjuUR9Xv6dgSMDOxDI6e1kbm7ARacA296BIoEgD7qs6n%2F4QwHUIQKGsQRQuJn%2BjfQ5fLpcvinkS%0AlhWv4myDksUJm9wt6ZzKo4efWnscrH5wMILLElPUHy%2Bu1kIU8sRh%0A";
        //Log.e("guoinfo", "content:"+ Uri.decode(content));
        String result = JunSEncrypt.decryptDInfo("mpay.junshanggame.com", "junshang", Uri.decode(content));
        //Log.e("guoinfo", "result:"+result);
        textView.setText(result);
    }
}