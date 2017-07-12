package com.shi.androidstudio.webviewandjs;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class JSoupHtmlActivity extends AppCompatActivity {

    String webContent = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
            "    <title></title>\n" +
            "\t<style type=\"text/css\">\n" +
            "\t\t*{\n" +
            "\t\tborder:none;\n" +
            "\t\tmargin:0;\n" +
            "\t\tpadding:0;\n" +
            "\t\tfont-size:12px;\n" +
            "\t\tfont-family:\"微软雅黑\";\n" +
            "\t}\n" +
            "\n" +
            "\t.price{\n" +
            "\t\twidth:100%;\n" +
            "\t\theight:50px;\n" +
            "\t\tmargin-top:30px;\n" +
            "\t}\n" +
            "\n" +
            "\t.price p{\n" +
            "\t\tfont-size:18px;\n" +
            "\t\tcolor:#ec5542;\n" +
            "\t\tline-height:50px;\n" +
            "\t\ttext-align:center;\n" +
            "\t\tfont-weight:bold;\n" +
            "\t}\n" +
            "\n" +
            "\t.price p span{\n" +
            "\t\tfont-size:18px;\n" +
            "\t}\n" +
            "\n" +
            "\t.qdCode{\n" +
            "\t\twidth:80%;\n" +
            "\t\theight:auto;\n" +
            "\t\tmargin:0px auto;\n" +
            "\t}\n" +
            "\n" +
            "\t.qdCode img{\n" +
            "\t\twidth:100%;\n" +
            "\t}\n" +
            "\t</style>\n" +
            "\t<script>\n" +
            "\t\tfunction changetext(id)\n" +
            "\t\t\t{\n" +
            "\t\t\talert(\"文字内容\");\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\tfunction jsJava(){\n" +
            "\t\t\t//调用java的方法，顶级对象，java方法\n" +
            "\t\t\t//androidObject，这是因为androidObject挂载到js的window对象下了\n" +
            "\t\t\tandroidObject.showToast(\"我是被JS执行起来的代码\");\n" +
            "\t\t}\n" +
            "\t</script>\n" +
            "</head>\n" +
            "<body>\n" +
            "\t<div class=\"price\" onclick=\"jsJava()\" >\n" +
            "\t<p >付款金额：￥<span>2600</span></p>\n" +
            "\t</div>\n" +
            "\t<div class=\"qdCode\" onclick=\"changetext(this)\" >\n" +
            "\t\t<img src=\"file:///android_asset/liantu.png\" id=\"qdCode\"/>\n" +
            "\t</div>\n" +
            "</body>\n" +
            "</html>";


    private static final String DEFAULT_URL = "file:///android_asset/test.html";

    Document document;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsoup_html);
        webView = (WebView) findViewById(R.id.webView);
        webView.loadDataWithBaseURL(null, webContent, "text/html", "utf-8", null);
        initView();
        initData();
    }

    private void initData() {
        document = Jsoup.parse(webContent);
        Elements elements = document.getElementsByClass("price");
        elements.get(0).html("<p>测试数据</p>");
        Element element = document.getElementById("qdCode");
        element.attr("src","http://img.ui.cn/data/file/9/1/5/44519.gif");
        String body = document.toString();
        webView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
    }

    private void initView() {

        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

                AlertDialog.Builder builder = new AlertDialog.Builder(JSoupHtmlActivity.this);
                builder.setTitle("提示");
                builder.setMessage(message);//这个message就是alert传递过来的值
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //处理确定按钮了
                        result.confirm();//通过jsresult传递，告诉js点击的是确定按钮
                    }
                });
                builder.show();

                return true;
            }

        });

        webView.getSettings().setJavaScriptEnabled(true);//Webview支持js代码

        webView.addJavascriptInterface(new Object(){
            //定义要调用的方法
            //msg由js调用的时候传递
            @JavascriptInterface
            public void showToast(String msg){
                Toast.makeText(getApplicationContext(),
                        msg, Toast.LENGTH_SHORT).show();
            }
        }, "androidObject");


    }
}
