 ### JS和Android交互调用

>Android开发过程中，我们或多或少都会用到webview，使用webview来展示一些经常变动的界面更加方便简单，也更易于维护。在使用webview来展示网页的时候，有些时候我们需要通过JS和Android原生控件进行交互，以实现自己需要的效果或功能，本文通过一个demo简单实现了JS和Android原生控件的交互。

#### 一、webView加载页面

我们都知道在Android中是通过webView来加载html页面的，根据HTML文件所在的位置不同写法也不同：

- 例如：加载assets文件夹下的test.html页面
```
mWebView.loadUrl("file:///android_asset/test.html")
```

- 例如：加载网页
```
mWebView.loadUrl("http://www.baidu.com")
```

如果只是这样调用mWebView.loadUrl()加载的话,那么当你点击页面中的链接时，页面将会在你手机默认的浏览器上打开。那如果想要页面在App内中打开的话，那么就得设置setWebViewClient：

```java
mWebView.setWebViewClient(new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //我们可以在这里拦截特定的rl请求，然后进行自己要的操作
                if (url.equals("file:///android_asset/test2.html")) {
                    Log.e(TAG, "shouldOverrideUrlLoading: " + url);
                    startActivity(new Intent(MainActivity.this,Main2Activity.class));
                    return true;
                } else {
                //这里我们自己重新加载新的url页面，防止点击链接跳转到系统浏览器
                    mWebView.loadUrl(url);
                    return true;
                }
            }
        }
    });
```

重写Activity的onBackPressed方法，使得返回按钮不会关闭当前页面，而是返回webview的上一个历史页面:

```java
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            //返回上一个页
            webView.goBack();
            return ;
        }
        super.onBackPressed();
    }
```


#### 二、给webView添加加载新页面的进度条。

- 开启和关闭进度条：

        webView.setWebViewClient(new WebViewClient() {

            //重写页面打开和结束的监听。打开时弹出菊花，关闭时隐藏菊花
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
             * 界面打开完毕的回调
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                //隐藏菊花:不为空，正在显示。才隐藏关闭
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

        });

- 让进度条显示页面加载进度：

```java
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
```

#### 三、Android本地通过Java调用HTML页面中的JavaScript方法

想要调用js方法那么就必须让webView支持

    //首先设置Webview支持JS代码
    webView.getSettings().setJavaScriptEnabled(true);

**若调用的js方法没有返回值，则直接可以调用mWebView.loadUrl("javascript:do()");其中do是js中的方法；若有返回值时我们可以调用mWebView.evaluateJavascript()方法：**

```
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
```

对应的JS方法:

```
    function sum(a,b){
    return a+b;
    }

    function showInfoFromJava(){
    document.getElementById("p").innerHTML="Java成功调的JS方法";
    }
```


#### 四、js调用Android本地Java方法

在Android4.2以上可以直接使用@JavascriptInterface注解来声明，下面是在一个本地Java方法

```
    public void addJavascriptInterface(Object object, String name);
```

- 1.object参数：在object对象里面添加我们想要在JS里面调用的Android方法，下面的代码中我们调用了showToast方法
- 2.name参数：这里的name就是我们可以在JS里面调用的对象名称，对应下面代码中的JSTest

对应的JS代码:

```js
	function jsJava(){
		//调用java的方法，顶级对象，java方法
		//可以直接访问JSTest，这是因为JSTest挂载到js的window对象下了
		JSTest.showToast("我是被JS执行的Android代码");
	}
```

对应的Java代码：

```
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
```


#### 五、重绘alert、confirm和prompt的弹出效果，并把用户具体的操作结果回调给JS

- alert弹窗：

![](picture\alert.png)

- 重绘confirm弹窗：

![](picture\confirm.png)

- 重绘prompt弹窗：

![](picture\prompt.png)

- 具体代码：

```java
            /**
             * Webview加载html中有alert()执行的时候，会回调这个方法
             * url:当前Webview显示的url
             * message：alert的参数值
             * JsResult：java将结果回传到js中
             */
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(JSActivity.this);
                builder.setTitle("提示:看到这个，说明Java成功重写了Js的Alert方法");
                builder.setMessage(message);//这个message就是alert传递过来的值
                builder.setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //处理确定按钮，且通过jsresult传递，告诉js点击的是确定按钮
                        result.confirm();
                    }
                });
                builder.show();
                //自己处理
                result.cancel();
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
                builder.setTitle("提示:看到这个，说明Java成功重写了Js的Confirm方法");
                builder.setMessage(message);//这个message就是alert传递过来的值
                builder.setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //处理确定按钮，且通过jsresult传递，告诉js点击的是确定按钮
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
                builder.show();
                //自己处理
                result.cancel();
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
                builder.show();
                //自己处理
                result.cancel();
                return true;
            }
        });
```

#### 五、效果图以及代码布局文件：

![效果图](picture\GIF.gif)

界面上方是两个按钮，下方是一个webview控件，开启页面自动加载url，这里为了方便学习，
我已经写了一个html文件放置在了Asset文件中，通过 file:///android_asset/test.html 来进行加载。
webview成功加载页面后，会出现四个新的按钮，点击不同的按钮，会产生不同的效果。

- **asset文件夹下面的test.html文件**

``` java
<html>
<head>
    <meta charset="UTF-8">
    <title>Document</title>
    <script>

	function jsAlert(){
		var r = alert("我是Alert的提示框");
		document.getElementById("p").innerHTML="Java成功调的JS的alert方法";
	}

	function jsConFirm(){
      var r = confirm("我是ConFirm的弹出框");
      if (r == true)
      {
		document.getElementById("p").innerHTML="用户点击了确认按钮";
      }else{
		document.getElementById("p").innerHTML="用户点击了取消按钮";
      }
	}
	function jsPrompt(){
		//第一个参数是提示
		//第二个参数是默认值
		var r = prompt("请输入姓名：","小明同学");
		if (r != null)
        {
		 document.getElementById("p").innerHTML="用户输入的姓名为："+r;
        }else{
		 document.getElementById("p").innerHTML="用户点击了取消按钮";
        }

	}
	function jsJava(){
		//调用java的方法，顶级对象，java方法
		//可以直接访问JSTest，这是因为JSTest挂载到js的window对象下了
		JSTest.showToast("我是被JS执行的Android代码");
	}

    function sum(a,b){
    return a+b;
    }

    function showInfoFromJava(){
    document.getElementById("p").innerHTML="JS方法成功被Java调用";
    }

    </script>
</head>
<body>

<h3 id="p">界面成功初始化</h3>

<h5 onclick="jsAlert()">调用jsAlert方法</h5>
<input type="button" value="开启Alert提示框" onclick="jsAlert()"/>
<h5 onclick="jsAlert()">调用jsConFirm方法</h5>
<input type="button" value="开启ConFirm弹窗" onclick="jsConFirm()"/>
<h5 onclick="jsAlert()">调用jsPrompt方法</h5>
<input type="button" value="开启Prompt弹窗" onclick="jsPrompt()"/>
<h5 onclick="jsAlert()">调用jsJava方法</h5>
<input type="button" value="调用Java方法" onclick="jsJava()"/>

</body>
</html>

```

- **具体的java代码**

```java
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
```

-  **布局文件activity_js.xml**

```java
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".JSoupHtmlActivity">


    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:onClick="onSum"
        android:text="Java调用JS的Sum方法"
        tools:ignore="OnClick" />

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:onClick="onDoing"
        android:text="Java调用JS的  showInfoFromJava 方法"
        tools:ignore="OnClick" />

    <WebView
        android:id="@+id/webView"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent" />

</LinearLayout>
```

代码过程描述的废话我就不多说了，注释写的算是比较仔细了，另外再强调两点需要注意的地方：

### 1、不要忘记通过setJavaScriptEnabled(true)设置webview支持JS代码

### 2、在使用addJavascriptInterface方法添加挂载对象时，要注意在Android4.2之后需要给对象方法加上@JavascriptInterface注解。

### 3、重绘alert、confirm和prompt的弹出效果之后，在对话框结束之后一定要调用result.confirm()或者result.cancel()两个方法中的一个
，否则会出现后续再次点击html页面按钮，页面无响应的情况

项目地址:[传送门]()