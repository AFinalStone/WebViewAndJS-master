package com.shi.androidstudio.webviewandjs;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class JSActivity extends AppCompatActivity {

    //assets下的文件的test.html所在的绝对路径
    private static final String DEFAULT_URL = "file:///android_asset/test.html";

    private WebView webView;

    private ProgressDialog progressDialog;//加载界面的菊花

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js);
        initView();
        initWebView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(DEFAULT_URL);
    }

    /**
     * 初始化webview
     */
    private void initWebView() {

        //首先设置Webview支持JS代码
        webView.getSettings().setJavaScriptEnabled(true);

        //Webview自己处理超链接(Webview的监听器非常多，封装一个特殊的监听类来处理)
        webView.setWebViewClient(new WebViewClient() {

            /**
             * 当打开超链接的时候，回调的方法
             * WebView：自己本身webView
             * url：即将打开的url
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //自己处理新的url
                webView.loadUrl(url);
                //true就是自己处理
                return true;
            }

            //重写页面打开和结束的监听。打开时弹出菊花
            /**
             * 界面打开的回调
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                //弹出菊花
                progressDialog = new ProgressDialog(JSActivity.this);
                progressDialog.setTitle("提示");
                progressDialog.setMessage("软软正在拼命加载……");
                progressDialog.show();

            }

            /**
             * 重写页面打开和结束的监听。打开时弹出菊花，关闭时隐藏菊花
             * 界面打开完毕的回调
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                //隐藏菊花:不为空，正在显示。才隐藏
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

        });

        //设置进度条
        //WebChromeClient与webViewClient的区别
        //webViewClient处理偏界面的操作：打开新界面，界面打开，界面打开结束
        //WebChromeClient处理偏js的操作
        webView.setWebChromeClient(new WebChromeClient() {
            /**
             * 进度改变的回调
             * WebView：就是本身
             * newProgress：即将要显示的进度
             */
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.setMessage("软软正在拼命加载……" + newProgress + "%");
            }

            /**
             * 重写alert、confirm和prompt的弹出效果，并把用户操作的结果回调给JS
             */
            /**
             * Webview加载html中有alert()执行的时候，会回调这个方法
             * url:当前Webview显示的url
             * message：alert的参数值
             * JsResult：java将结果回传到js中
             */
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(JSActivity.this);
                builder.setTitle("提示:看到这个，说明Java成功重写了Js的Alert方法");
                builder.setMessage(message);//这个message就是alert传递过来的值
                builder.setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //处理确定按钮了，且通过jsresult传递，告诉js点击的是确定按钮
                        result.confirm();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //防止用户点击对话框外围，再次点击按钮页面无响应
                        result.cancel();
                    }
                });
                builder.show();
                //自己处理
                return true;
            }

            /**
             * Webview加载html中有confirm执行的时候，会回调这个方法
             * url:当前Webview显示的url
             * message：alert的参数值
             * JsResult：java将结果回传到js中
             */
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(JSActivity.this);
                builder.setTitle("提示:" +
                        "看到这个，说明Java成功重写了Js的Confirm方法");
                builder.setMessage(message);//这个message就是alert传递过来的值
                builder.setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //处理确定按钮了，且通过jsresult传递，告诉js点击的是确定按钮
                        result.confirm();
                    }
                });
                builder.setNegativeButton("取消", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //处理取消按钮，且通过jsresult传递，告诉js点击的是取消按钮
                        result.cancel();

                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //防止用户点击对话框外围，再次点击按钮页面无反应
                        result.cancel();
                    }
                });
                builder.show();
                //自己处理
                return true;
            }

            /**
             * Webview加载html中有prompt()执行的时候，会回调这个方法
             * url:当前Webview显示的url
             * message：alert的参数值
             *defaultValue就是prompt的第二个参数值，输入框的默认值
             * JsPromptResult：java将结果重新回传到js中
             */
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                      final JsPromptResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(JSActivity.this);
                builder.setTitle("提示:看到这个，说明Java成功重写了Js的Prompt方法");
                builder.setMessage(message);//这个message就是alert传递过来的值
                //添加一个EditText
                final EditText editText = new EditText(JSActivity.this);
                editText.setText(defaultValue);//这个就是prompt 输入框的默认值
                //添加到对话框
                builder.setView(editText);
                builder.setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取edittext的新输入的值
                        String newValue = editText.getText().toString().trim();
                        //处理确定按钮了，且过jsresult传递，告诉js点击的是确定按钮(参数就是输入框新输入的值，我们需要回传到js中)
                        result.confirm(newValue);
                    }
                });
                builder.setNegativeButton("取消", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //处理取消按钮，且过jsresult传递，告诉js点击的是取消按钮
                        result.cancel();

                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //防止用户点击对话框外围，再次点击按钮页面无反应
                        result.cancel();
                    }
                });
                builder.show();
                //自己处理
                return true;
            }
        });

        //java与js回调，自定义方法
        //1.java调用js
        //2.js调用java
        //首先java暴露接口，供js调用
        /**
         * obj:暴露的要调用的对象
         * interfaceName：对象的映射名称 ,object的对象名，在js中可以直接调用
         * 在html的js中：JSTest.showToast(msg)
         * 可以直接访问JSTest，这是因为JSTest挂载到js的window对象下了
         */
        webView.addJavascriptInterface(new Object() {
            //定义要调用的方法
            //msg由js调用的时候传递
            @JavascriptInterface
            public void showToast(String msg) {
                Toast.makeText(getApplicationContext(),
                        msg, Toast.LENGTH_SHORT).show();
            }
        }, "JSTest");

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onSum(View view){
        webView.evaluateJavascript("sum(1,2)", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Toast.makeText(getApplicationContext(),
                        "相加结果："+value, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDoing(View view){
        String msg = "测试";
        webView.loadUrl("javascript:showInfoFromJava('"+msg+"')");
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            //返回上一个页
            webView.goBack();
            return ;
        }
        super.onBackPressed();
    }

}
